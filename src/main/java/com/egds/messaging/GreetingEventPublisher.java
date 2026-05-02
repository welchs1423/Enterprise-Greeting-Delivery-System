package com.egds.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer component that publishes {@link GreetingEvent} instances
 * to the EGDS greeting event topic.
 *
 * <p>Delivery is fully asynchronous. The returned
 * {@link CompletableFuture} resolves to a {@link SendResult} containing
 * partition and offset metadata upon broker acknowledgment, or completes
 * exceptionally if the broker cannot be reached within the timeout.
 *
 * <p>The event's {@code correlationId} is used as the Kafka message key
 * to ensure partition-level ordering within the same correlation context.
 */
@Component
public class GreetingEventPublisher {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingEventPublisher.class);

    /** Kafka template for producing GreetingEvent messages. */
    private final KafkaTemplate<String, GreetingEvent> kafkaTemplate;

    /** Target Kafka topic name, resolved from application properties. */
    private final String greetingTopic;

    /**
     * @param template the Kafka template for producing messages
     * @param topic    the greeting event topic name
     */
    public GreetingEventPublisher(
            final KafkaTemplate<String, GreetingEvent> template,
            @Value("${egds.kafka.topic.greeting}") final String topic) {
        this.kafkaTemplate = template;
        this.greetingTopic = topic;
    }

    /**
     * Publishes a {@link GreetingEvent} to the greeting event topic.
     * Delivery confirmation and error handling are performed asynchronously
     * via a {@code whenComplete} callback on the returned future.
     *
     * @param event the event to publish; must not be null
     * @return a {@link CompletableFuture} that resolves to the
     *         {@link SendResult} on broker acknowledgment
     */
    public CompletableFuture<SendResult<String, GreetingEvent>> publish(
            final GreetingEvent event) {
        LOG.info("[KAFKA] Publishing GreetingEvent topic={} key={}",
                greetingTopic, event.getCorrelationId());

        CompletableFuture<SendResult<String, GreetingEvent>> future =
                kafkaTemplate.send(
                        greetingTopic, event.getCorrelationId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error("[KAFKA] Publish failed topic={} key={} error={}",
                        greetingTopic,
                        event.getCorrelationId(),
                        ex.getMessage());
            } else {
                LOG.info("[KAFKA] Publish confirmed"
                        + " topic={} key={} partition={} offset={}",
                        greetingTopic,
                        event.getCorrelationId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return future;
    }
}
