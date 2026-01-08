package ru.yandex.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.time.Instant;
import java.util.Map;

import static ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import static ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro.LUMINOSITY;
import static ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro.MOTION;
import static ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro.SWITCH;
import static ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro.TEMPERATURE;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SnapshotAnalyzer {
    private final ScenarioRepository scenarioRepository;
    private final HubRouterControllerBlockingStub hubRouterClient;

    public SnapshotAnalyzer(ScenarioRepository scenarioRepository,
                            @GrpcClient("hub-router") HubRouterControllerBlockingStub hubRouterClient) {
        this.scenarioRepository = scenarioRepository;
        this.hubRouterClient = hubRouterClient;
    }

    public void processing(SensorsSnapshotAvro snapshot) {
        log.trace("Analysis of the hub snapshot: {}", snapshot.getHubId());

        scenarioRepository.findByHubId(snapshot.getHubId())
                .stream()
                .filter(scenario -> isConditionsMatchSnapshot(snapshot, scenario.getConditions()))
                .forEach(this::performActions);
    }

    private boolean isConditionsMatchSnapshot(SensorsSnapshotAvro snapshot, Map<String, Condition> conditions) {
        return conditions.entrySet().stream()
                .allMatch(entry -> checkCondition(entry.getKey(), entry.getValue(), snapshot));
    }

    private boolean checkCondition(String sensorId, Condition condition, SensorsSnapshotAvro snapshot) {
        SensorStateAvro state = snapshot.getSensorsState().get(sensorId);
        if (state == null) return false;
        Integer valueToCheck = extractValue(state.getData(), condition.getType());
        return valueToCheck != null && condition.checkValue(valueToCheck);
    }

    private Integer extractValue(Object payload, ConditionTypeAvro type) {
        return switch (payload) {
            case ClimateSensorAvro p -> switch (type) {
                case TEMPERATURE -> p.getTemperatureC();
                case HUMIDITY -> p.getHumidity();
                case CO2LEVEL -> p.getCo2Level();
                default -> null;
            };
            case TemperatureSensorAvro p -> type == TEMPERATURE ? p.getTemperatureC() : null;
            case LightSensorAvro p -> type == LUMINOSITY ? p.getLuminosity() : null;
            case MotionSensorAvro p -> type == MOTION ? (p.getMotion() ? 1 : 0) : null;
            case SwitchSensorAvro p -> type == SWITCH ? (p.getState() ? 1 : 0) : null;
            default -> null;
        };
    }

    private void performActions(Scenario scenario) {
        log.info("The script is being executed: {}", scenario.getName());

        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(Instant.now().getEpochSecond())
                .setNanos(Instant.now().getNano())
                .build();

        for (Map.Entry<String, Action> entry : scenario.getActions().entrySet()) {
            String sensorId = entry.getKey();
            Action action = entry.getValue();
            try {
                ActionTypeProto protoType = ActionTypeProto.valueOf(action.getType().name());
                DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                        .setSensorId(sensorId)
                        .setType(protoType);
                if (action.getValue() != null) {
                    actionBuilder.setValue(action.getValue());
                }
                hubRouterClient.handleDeviceAction(DeviceActionRequest.newBuilder()
                        .setHubId(scenario.getHubId())
                        .setScenarioName(scenario.getName())
                        .setAction(actionBuilder.build())
                        .setTimestamp(timestamp)
                        .build());
                log.info("An action has been sent to the router: sensor {}, type {}, value {}",
                        sensorId, protoType, action.getValue());
            } catch (Exception ex) {
                log.error("Error sending script action {}: {}", scenario.getName(), ex.getMessage(), ex);
            }
        }
    }
}