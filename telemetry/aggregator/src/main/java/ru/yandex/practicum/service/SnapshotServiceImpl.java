package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {
    private final Map<String, SensorsSnapshotAvro> sensors = new HashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        final SensorsSnapshotAvro snapshot = sensors.computeIfAbsent(
                event.getHubId(),
                hubId -> {
                    SensorsSnapshotAvro newSnapshot = new SensorsSnapshotAvro();
                    newSnapshot.setHubId(hubId);
                    newSnapshot.setTimestamp(event.getTimestamp());
                    newSnapshot.setSensorsState(new HashMap<>());
                    return newSnapshot;
                }
        );

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        if (sensorsState.containsKey(event.getId())) {
            SensorStateAvro oldState = sensorsState.get(event.getId());
            if (oldState.getTimestamp().isAfter(event.getTimestamp()) ||
                    oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(event.getTimestamp());
        newState.setData(event.getPayload());

        sensorsState.put(event.getId(), newState);
        snapshot.setTimestamp(newState.getTimestamp());

        return Optional.of(snapshot);
    }
}