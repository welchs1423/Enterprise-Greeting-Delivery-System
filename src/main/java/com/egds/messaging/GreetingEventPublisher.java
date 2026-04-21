package com.egds.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer component responsible for publishing {@link GreetingEvent} instances
 * to the EGDS greeting event topic.
 *
 * <p>Delivery is fully asynchronous. The returned {@link CompletableFuture} resolves to
 * a {@link SendResult} containing partition and offset metadata upon broker acknowledgment,
 * or completes exceptionally if the broker cannot be reached within the configured timeout.
 *
 * <p>The event's {@code correlationId} is used as the Kafka message key to ensure
 * partition-level ordering for events sharing the same correlation context.
 */
@Component
public class GreetingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(GreetingEventPublisher.class);

    private final KafkaTemplate<String, GreetingEvent> kafkaTemplate;
    private final String greetingTopic;

    public GreetingEventPublisher(
            KafkaTemplate<String, GreetingEvent> kafkaTemplate,
            @Value("${egds.kafka.topic.greeting}") String greetingTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.greetingTopic = greetingTopic;
    }

    /**
     * Publishes a {@link GreetingEvent} to the configured greeting event topic.
     * Delivery confirmation and error handling are performed asynchronously via
     * a {@code whenComplete} callback on the returned future.
     *
     * @param event the event to publish; must not be null
     * @return a {@link CompletableFuture} that resolves to the {@link SendResult}
     *         on broker acknowledgment
     */
    public CompletableFuture<SendResult<String, GreetingEvent>> publish(GreetingEvent event) {
        log.info("[KAFKA] Publishing GreetingEvent topic={} key={}",
                greetingTopic, event.getCorrelationId());

        CompletableFuture<SendResult<String, GreetingEvent>> future =
                kafkaTemplate.send(greetingTopic, event.getCorrelationId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[KAFKA] Publish failed topic={} key={} error={}",
                        greetingTopic, event.getCorrelationId(), ex.getMessage());
            } else {
                log.info("[KAFKA] Publish confirmed topic={} key={} partition={} offset={}",
                        greetingTopic,
                        event.getCorrelationId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return future;
    }
}
