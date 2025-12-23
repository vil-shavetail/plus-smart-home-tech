package ru.yandex.practicum.service.handler.hub;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.config.KafkaConfig;
import ru.yandex.practicum.model.hub.HubEvent;

@Slf4j
@AllArgsConstructor
public abstract class BaseHubEventHandler <T extends SpecificRecordBase> implements HubEventHandler {
    protected final KafkaConfig.KafkaEventProducer producer;
    protected final KafkaConfig topics;

    protected abstract T mapToAvro(HubEvent event);

    @Override
    public void handle(HubEvent event) {
        T avroEvent = mapToAvro(event);
        String topic = topics.producer.getTopics().get(KafkaConfig.TopicType.HUBS_EVENTS);
        log.info("Send event {} -> topic {}", getMessageType(), topic);
        producer.send(topic, event.getHubId(), avroEvent);
    }
}
