package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.kafka.config.KafkaConfig;
import ru.yandex.practicum.model.Sensor;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class HubEventProcessor implements Runnable {
    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final SensorService sensorService;
    private final ScenarioService scenarioService;
    private final List<String> topics;
    private final Duration pollTimeout;

    public HubEventProcessor(KafkaConfig config, SensorService sensorService, ScenarioService scenarioService) {
        this.sensorService = sensorService;
        this.scenarioService = scenarioService;
        final KafkaConfig.ConsumerConfig consumerConfig = config.getConsumers().get(this.getClass().getSimpleName());
        if (consumerConfig == null) {
            throw new IllegalStateException("The consumer configuration was not found: " + this.getClass().getSimpleName());
        }
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.topics = consumerConfig.getTopics();
        this.pollTimeout = consumerConfig.getPollTimeout();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Completion of the JVM. I'm interrupting consumer's work");
            consumer.wakeup();
        }, "KafkaShutdownHook-" + this.getClass().getSimpleName()));
    }

    @Override
    public void run() {
        log.info("The consumer tag subscribes to topics: {}", topics);
        consumer.subscribe(topics);
        try {
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(pollTimeout);
                if (records.isEmpty()) {
                    log.trace("There are no new entries. Expectation");
                    continue;
                }
                log.debug("Getting {} records", records.count());
                records.forEach(record -> {
                    try {
                        processEvent(record.value());
                    } catch (Exception ex) {
                        log.error("Error when processing a record from the topic: {}, partition: {}, offset: {}",
                                record.topic(), record.partition(), record.offset(), ex);
                    }
                });
                try {
                    consumer.commitSync();
                    log.trace("Offsets are fixed");
                } catch (Exception ex) {
                    log.error("Offset fixing error", ex);
                }
            }
        } catch (WakeupException ex) {
            log.info("Consumer shutdown");
        } catch (Exception ex) {
            log.error("Error during consumer operation", ex);
        } finally {
            log.info("Closing consumer");
            consumer.close();
        }
    }

    private void processEvent(HubEventAvro hubEvent) {
        String hubId = hubEvent.getHubId();
        switch (hubEvent.getPayload()) {
            case DeviceAddedEventAvro deviceAddedEventAvro -> processEvent(hubId, deviceAddedEventAvro);
            case DeviceRemovedEventAvro deviceRemovedEventAvro -> processEvent(hubId, deviceRemovedEventAvro);
            case ScenarioAddedEventAvro scenarioAddedEventAvro -> processEvent(hubId, scenarioAddedEventAvro);
            case ScenarioRemovedEventAvro scenarioRemovedEventAvro -> processEvent(hubId, scenarioRemovedEventAvro);
            default -> log.warn("An unknown type of event was received: {}", hubEvent);
        }
    }

    private void processEvent(String hubId, DeviceAddedEventAvro event) {
        String deviceId = event.getId();
        Optional<Sensor> maybeAdded = sensorService.findByIdAndHubId(hubId, deviceId);
        if (maybeAdded.isPresent()) {
            log.warn("The device with the id {} has already been registered in the hub {}", deviceId, hubId);
            return;
        }
        Sensor sensor = new Sensor();
        sensor.setHubId(hubId);
        sensor.setId(deviceId);
        log.info("A new sensor {} has been registered in the hub: {}", deviceId, hubId);
        sensorService.save(sensor);
    }

    private void processEvent(String hubId, DeviceRemovedEventAvro event) {
        String deviceId = event.getId();
        log.info("Removing the sensor {} from the hub {}", deviceId, hubId);
        sensorService.findByIdAndHubId(hubId, deviceId)
                .ifPresentOrElse(
                        sensorService::delete,
                        () -> log.warn("The sensor {} was not found in the hub {}. Deletion is not required", deviceId, hubId)
                );
    }

    private void processEvent(String hubId, ScenarioAddedEventAvro event) {
        log.info("Request to add a script '{}' for the hub {}", event.getName(), hubId);
        scenarioService.save(event, hubId);
    }

    private void processEvent(String hubId, ScenarioRemovedEventAvro event) {
        log.info("Request to delete the script '{}' from the hub {}", event.getName(), hubId);
        scenarioService.delete(event.getName(), hubId);
    }
}