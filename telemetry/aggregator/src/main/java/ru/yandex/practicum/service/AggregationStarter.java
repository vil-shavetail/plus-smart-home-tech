package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.config.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationStarter {
    private final SnapshotService snapshotService;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaConfig.ConsumerConfig consumerConfig;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final KafkaConfig.ProducerConfig producerConfig;

    @Autowired
    public AggregationStarter(SnapshotService snapshotService, KafkaConfig kafkaConfig) {
        this.snapshotService = snapshotService;
        this.consumerConfig = kafkaConfig.getConsumer();
        this.producerConfig = kafkaConfig.getProducer();
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.producer = new KafkaProducer<>(producerConfig.getProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("The JVM termination hook was triggered. Interrupting the work of the consumer.");
            consumer.wakeup();
        }));
    }

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */
    public void start() {
        try {
            log.trace("Subscribing to the topic {} to receive events from sensors", consumerConfig.getTopic());
            consumer.subscribe(List.of(consumerConfig.getTopic()));
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(consumerConfig.getPollTimeout());
                if(!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<String, SensorEventAvro> record : records) {
                        log.trace("Processing a message from the hub {} from the partition {}, offset: {}",
                                record.key(), record.partition(), record.offset());

                        handleEvent(record.value());
                        manageOffsets(record, count++);
                    }
                    producer.flush();
                    consumer.commitAsync();
                }
            }
        } catch (WakeupException ignored) {
        } catch (Exception ex) {
            log.error("Sensor event processing time error: ", ex);
        } finally {
            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Closing the consumer");
                consumer.close();
                log.info("Closing the producer");
                producer.close();
            }
        }
    }

    private void handleEvent(SensorEventAvro event) {
        Optional<SensorsSnapshotAvro> updatedState = snapshotService.updateState(event);
        if(updatedState.isPresent()) {
            SensorsSnapshotAvro sensorsSnapshot = updatedState.get();
            log.info("An event from the sensor {} has updated the snapshot status. Saving a snapshot of the status of the hub sensor {} from {} to the topic {}",
                    event.getId(), sensorsSnapshot.getHubId(), sensorsSnapshot.getTimestamp(),
                    producerConfig.getTopic());

            ProducerRecord<String, SensorsSnapshotAvro> recordToSend =
                    new ProducerRecord<>(
                            producerConfig.getTopic(),
                            null,
                            sensorsSnapshot.getTimestamp().toEpochMilli(),
                            sensorsSnapshot.getHubId(),
                            sensorsSnapshot
                    );

            Future<RecordMetadata> futureResult = producer.send(recordToSend);
            producer.flush();
            try {
                RecordMetadata metadata = futureResult.get();
                log.info("Snapshot saved in partition {}, offset {}", metadata.partition(), metadata.offset());
            } catch (InterruptedException | ExecutionException ex) {
                log.warn("Couldn't add a snapshot to the topic {}", producerConfig.getTopic(), ex);
            }
        } else {
            log.trace("The event from the sensor {} of the hub {} did not update the snapshot status", event.getId(), event.getHubId());
        }
    }

    private void manageOffsets(ConsumerRecord<String, SensorEventAvro> record, int count) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if(count % 200 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if(exception != null) {
                    log.warn("Offset commit time error: {}", offsets, exception);
                }
            });
        }
    }
}