package ru.yandex.practicum.service.handler.hub;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.producer.KafkaEventProducer;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Service
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {
    public ScenarioAddedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventProto event) {
        ScenarioAddedEventProto proto = event.getScenarioAdded();

        return new ScenarioAddedEventAvro(
                proto.getName(),
                proto.getConditionList().stream().map(this::mapConditionToAvro).toList(),
                proto.getActionList().stream().map(this::mapActionToAvro).toList()
        );
    }

    private ScenarioConditionAvro mapConditionToAvro(ScenarioConditionProto condition) {
        return new ScenarioConditionAvro(
                condition.getSensorId(),
                ConditionTypeAvro.valueOf(condition.getType().name()),
                ConditionOperationAvro.valueOf(condition.getOperation().name()),
                condition.getValueCase().equals(ScenarioConditionProto.ValueCase.BOOL_VALUE) ? condition.getBoolValue() : condition.getIntValue()
        );
    }

    private DeviceActionAvro mapActionToAvro(DeviceActionProto action) {
        return new DeviceActionAvro(
                action.getSensorId(),
                ActionTypeAvro.valueOf(action.getType().name()),
                action.getValue()
        );
    }
}