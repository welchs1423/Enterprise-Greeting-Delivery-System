package com.egds.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka infrastructure configuration for the EGDS platform.
 * Defines the greeting event topic with production-grade partition settings.
 *
 * <p>Topic creation via the {@link NewTopic} bean is idempotent:
 * existing topics with matching configuration are not modified.
 * Three partitions are provisioned to allow horizontal consumer scaling.
 */
@Configuration
public class KafkaConfig {

    /** Number of partitions for the greeting event topic. */
    private static final int GREETING_TOPIC_PARTITIONS = 3;

    /** Greeting event topic name, resolved from application properties. */
    @Value("${egds.kafka.topic.greeting}")
    private String greetingTopic;

    /**
     * Declares the EGDS greeting event topic.
     *
     * @return the topic definition for the greeting event channel
     */
    @Bean
    public NewTopic greetingEventTopic() {
        return TopicBuilder.name(greetingTopic)
                .partitions(GREETING_TOPIC_PARTITIONS)
                .replicas(1)
                .build();
    }
}
