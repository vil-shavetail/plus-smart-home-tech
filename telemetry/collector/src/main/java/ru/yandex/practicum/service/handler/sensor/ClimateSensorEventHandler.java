package ru.yandex.practicum.service.handler.sensor;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.producer.KafkaEventProducer;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;

@Service
public class ClimateSensorEventHandler extends BaseSensorEventHandler<ClimateSensorAvro> {
    public ClimateSensorEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    protected ClimateSensorAvro mapToAvro(SensorEventProto event) {
        ClimateSensorProto proto = event.getClimateSensor();

        return new ClimateSensorAvro(
                proto.getTemperatureC(),
                proto.getHumidity(),
                proto.getCo2Level()
        );
    }
}