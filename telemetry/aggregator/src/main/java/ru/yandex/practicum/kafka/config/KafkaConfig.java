package ru.yandex.practicum.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Properties;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties("aggregator.kafka")
public class KafkaConfig {
    private ProducerConfig producer;
    private ConsumerConfig consumer;

    @Setter
    @Getter
    @AllArgsConstructor
    public static class ProducerConfig {
        private String topic;
        private Properties properties;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class ConsumerConfig {
        private String topic;
        private Duration pollTimeout;
        private Properties properties;
    }
}