package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.config.KafkaConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SnapshotProcessor implements Runnable {
    private final SnapshotAnalyzer analyzer;
    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final List<String> topics;
    private final Duration pollTimeout;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public SnapshotProcessor(SnapshotAnalyzer analyzer, KafkaConfig kafkaConfig) {
        this.analyzer = analyzer;
        final String componentName = this.getClass().getSimpleName();
        final KafkaConfig.ConsumerConfig consumerConfig =
                kafkaConfig.getConsumers().get(componentName);
        if (consumerConfig == null) {
            throw new IllegalStateException("Consumer configuration not found: " + componentName);
        }
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.topics = consumerConfig.getTopics();
        this.pollTimeout = consumerConfig.getPollTimeout();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Completion of the JVM. Interrupting the work of the snapshot consumer");
            consumer.wakeup();
        }, "KafkaShutdownHook-" + componentName));
    }

    @Override
    public void run() {
        start();
    }

    public void start() {
        log.info("Launching a snapshot consumer. Subscribing to topics: {}", topics);
        try {
            consumer.subscribe(topics);
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(pollTimeout);
                if (records.isEmpty()) {
                    log.trace("There are no new entries. Expectation.");
                    continue;
                }
                log.debug("Processing {} snapshots.", records.count());
                int processedCount = 0;
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    try {
                        analyzer.processing(record.value());
                        updateOffsets(record);
                        processedCount++;
                        if (processedCount % 100 == 0) {
                            commitAsyncPeriodically();
                        }
                    } catch (Exception ex) {
                        log.error("Error when processing a record from the topic: {}, partition: {}, offset: {}",
                                record.topic(), record.partition(), record.offset(), ex);
                    }
                }
                consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Error when asynchronously fixing package offsets: {}", offsets, exception);
                    }
                });
            }
        } catch (WakeupException ignored) {
            log.info("Wakeup. Shutting down the work of the consumer");
        } catch (Exception ex) {
            log.error("Error during Kafka consumer operation", ex);
        } finally {
            try {
                log.info("Synchronous fixation of the remaining offsets before closing: {}", currentOffsets);
                consumer.commitSync(currentOffsets);
            } catch (Exception ex) {
                log.error("Error during the final synchronous commit of offsets", ex);
            } finally {
                log.info("Kafka-consumer closure");
                consumer.close();
            }
        }
    }

    private void updateOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        TopicPartition tp = new TopicPartition(record.topic(), record.partition());
        OffsetAndMetadata offset = new OffsetAndMetadata(record.offset() + 1);
        currentOffsets.put(tp, offset);
        log.trace("Updated the offset for {} on {}", tp, offset.offset());
    }

    private void commitAsyncPeriodically() {
        if (!currentOffsets.isEmpty()) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Error during offset fixing: {}", offsets, exception);
                } else {
                    log.debug("The offset commit {} is successful.", offsets.size());
                }
            });
        }
    }
}