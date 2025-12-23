package ru.yandex.practicum.service.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.config.KafkaConfig;
import ru.yandex.practicum.model.sensor.SensorEvent;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {
    protected final KafkaConfig.KafkaEventProducer producer;
    protected final KafkaConfig topics;
    protected abstract T mapToAvro(SensorEvent event);

    @Override
    public void handle(SensorEvent event) {
        T avroEvent = mapToAvro(event);
        String topic = topics.producer.getTopics().get(KafkaConfig.TopicType.SENSORS_EVENTS);

        log.info("Send event {} -> topic {}", getMessageType(), topic);
        producer.send(topic, event.getId(), avroEvent);
    }
}
