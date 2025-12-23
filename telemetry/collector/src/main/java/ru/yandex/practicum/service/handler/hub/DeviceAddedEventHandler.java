package ru.yandex.practicum.service.handler.hub;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.config.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.hub.enums.DeviceType;
import ru.yandex.practicum.model.hub.enums.HubEventType;

@Service
public class DeviceAddedEventHandler extends BaseHubEventHandler<DeviceAddedEventAvro> {
    public DeviceAddedEventHandler(KafkaConfig.KafkaEventProducer producer, KafkaConfig kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_ADDED_EVENT;
    }

    @Override
    protected DeviceAddedEventAvro mapToAvro(HubEvent event) {
        var deviceAddedEvent = (DeviceAddedEvent) event;

        return new DeviceAddedEventAvro(
                deviceAddedEvent.getId(),
                mapDeviceTypeToAvro(deviceAddedEvent.getDeviceType())
        );
    }

    private DeviceTypeAvro mapDeviceTypeToAvro(DeviceType deviceType) {
        return DeviceTypeAvro.valueOf(deviceType.name());
    }
}
