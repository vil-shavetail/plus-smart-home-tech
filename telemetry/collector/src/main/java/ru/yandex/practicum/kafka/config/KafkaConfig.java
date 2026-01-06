package ru.yandex.practicum.kafka.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties("collector.kafka")
public class KafkaConfig {
    private ProducerConfig producer;

    public enum TopicType {
        SENSORS_EVENTS, HUBS_EVENTS;

        public static TopicType from(String type) {
            for (TopicType value : values()) {
                if (value.name().equalsIgnoreCase(type.replace("-", "_"))) {
                    return value;
                }
            }
            return null;
        }
    }

    @Getter
    public static class ProducerConfig {
        public final Properties properties;
        private final EnumMap<TopicType, String> topics = new EnumMap<>(TopicType.class);

        public ProducerConfig(Properties properties, Map<String, String> topics) {
            this.properties = properties;
            for (Map.Entry<String, String> entry : topics.entrySet()) {
                this.topics.put(TopicType.from(entry.getKey()), entry.getValue());
            }
        }
    }
}