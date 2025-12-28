package ru.yandex.practicum.service.handler.hub;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.producer.KafkaEventProducer;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.model.hub.DeviceAction;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.model.hub.ScenarioCondition;
import ru.yandex.practicum.model.hub.enums.HubEventType;

@Service
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {
    public ScenarioAddedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEvent event) {
        var scenarioEvent = (ScenarioAddedEvent) event;

        return new ScenarioAddedEventAvro(
                scenarioEvent.getName(),
                scenarioEvent.getConditions().stream().map(this::mapConditionToAvro).toList(),
                scenarioEvent.getActions().stream().map(this::mapActionToAvro).toList()
        );
    }

    private ScenarioConditionAvro mapConditionToAvro(ScenarioCondition condition) {
        return new ScenarioConditionAvro(
                condition.getSensorId(),
                ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro.valueOf(condition.getType().name()),
                ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro.valueOf(condition.getOperation().name()),
                condition.getValue()
        );
    }

    private DeviceActionAvro mapActionToAvro(DeviceAction action) {
        return new DeviceActionAvro(
                action.getSensorId(),
                ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro.valueOf(action.getType().name()),
                action.getValue()
        );
    }
}