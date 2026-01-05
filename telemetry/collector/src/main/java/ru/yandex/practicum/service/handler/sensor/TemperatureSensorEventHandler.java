package ru.yandex.practicum.service.handler.sensor;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.producer.KafkaEventProducer;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Service
public class TemperatureSensorEventHandler extends BaseSensorEventHandler<TemperatureSensorAvro> {
    public TemperatureSensorEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    protected TemperatureSensorAvro mapToAvro(SensorEventProto event) {
        TemperatureSensorProto proto = event.getTemperatureSensor().getDefaultInstanceForType();

        TemperatureSensorAvro avro = new TemperatureSensorAvro();
        avro.setTemperatureC(proto.getTemperatureC());
        avro.setTemperatureF(proto.getTemperatureF());

        return avro;
    }
}