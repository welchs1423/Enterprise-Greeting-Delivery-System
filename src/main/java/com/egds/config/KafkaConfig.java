package com.egds.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka infrastructure configuration for the EGDS platform.
 * Defines the greeting event topic and the CQRS event-sourcing topic
 * with production-grade partition settings.
 *
 * <p>Topic creation via {@link NewTopic} beans is idempotent:
 * existing topics with matching configuration are not modified.
 * Three partitions are provisioned to allow horizontal consumer scaling.
 */
@Configuration
public class KafkaConfig {

    /** Number of partitions for all EGDS Kafka topics. */
    private static final int TOPIC_PARTITIONS = 3;

    /** Greeting event topic name (legacy delivery pipeline). */
    @Value("${egds.kafka.topic.greeting}")
    private String greetingTopic;

    /** CQRS event-sourcing topic name for GreetingRequestedEvent. */
    @Value("${egds.kafka.topic.greeting-requested}")
    private String greetingRequestedTopic;

    /**
     * Declares the EGDS legacy greeting event topic.
     * Consumed by {@link com.egds.messaging.GreetingEventConsumer}.
     *
     * @return the topic definition for the legacy delivery event channel
     */
    @Bean
    public NewTopic greetingEventTopic() {
        return TopicBuilder.name(greetingTopic)
                .partitions(TOPIC_PARTITIONS)
                .replicas(1)
                .build();
    }

    /**
     * Declares the CQRS event-sourcing topic.
     * Consumed by {@link com.egds.cqrs.projector.GreetingProjector}
     * to build the MongoDB materialized view.
     *
     * @return the topic definition for the CQRS event log channel
     */
    @Bean
    public NewTopic greetingRequestedEventTopic() {
        return TopicBuilder.name(greetingRequestedTopic)
                .partitions(TOPIC_PARTITIONS)
                .replicas(1)
                .build();
    }
}
