package com.egds.messaging;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
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
 *
 * <p>The publish path is guarded by three Resilience4j patterns applied
 * in order (outermost first):
 * <ol>
 *   <li>{@code @RateLimiter("kafkaPublish")} — caps throughput at
 *       100 events/s with a 2 s acquisition timeout; prevents broker
 *       flooding under request surge. Falls back to
 *       {@link #publishFallback}.</li>
 *   <li>{@code @CircuitBreaker("kafkaPublish")} — opens after 40%
 *       failure rate over 20 calls; prevents cascading broker
 *       unavailability. Falls back to {@link #publishFallback}.</li>
 *   <li>{@code @Retry("kafkaPublish")} — up to 3 attempts with
 *       exponential back-off (500 ms base, factor 2) for transient
 *       broker errors.</li>
 * </ol>
 *
 * <p>Each publish invocation creates an OpenTelemetry
 * {@link Span} tagged with the Kafka topic, partition key, and
 * correlation identifier for end-to-end trace continuity.
 */
@Component
public class GreetingEventPublisher {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingEventPublisher.class);

    /** Resilience4j instance name for all three patterns. */
    private static final String RESILIENCE_NAME = "kafkaPublish";

    /** OTel span name for Kafka publish operations. */
    private static final String SPAN_NAME = "egds.kafka-publish";

    /** Kafka template for producing GreetingEvent messages. */
    private final KafkaTemplate<String, GreetingEvent> kafkaTemplate;

    /** Target Kafka topic name, resolved from application properties. */
    private final String greetingTopic;

    /** Tracer used to create and manage publish spans. */
    private final Tracer tracer;

    /**
     * @param template the Kafka template for producing messages
     * @param topic    the greeting event topic name
     * @param otelTracer the Micrometer Tracing tracer
     */
    public GreetingEventPublisher(
            final KafkaTemplate<String, GreetingEvent> template,
            @Value("${egds.kafka.topic.greeting}") final String topic,
            final Tracer otelTracer) {
        this.kafkaTemplate = template;
        this.greetingTopic = topic;
        this.tracer = otelTracer;
    }

    /**
     * Publishes a {@link GreetingEvent} to the greeting event topic.
     * An OTel span is created for the duration of the synchronous
     * {@link KafkaTemplate#send} call. Delivery confirmation and error
     * handling are performed asynchronously via a {@code whenComplete}
     * callback on the returned future.
     *
     * <p>Decorated with {@code @RateLimiter}, {@code @CircuitBreaker},
     * and {@code @Retry} as documented in the class-level Javadoc.
     *
     * @param event the event to publish; must not be null
     * @return a {@link CompletableFuture} that resolves to the
     *         {@link SendResult} on broker acknowledgment, or completes
     *         exceptionally if all resilience patterns are exhausted
     */
    @RateLimiter(name = RESILIENCE_NAME, fallbackMethod = "publishFallback")
    @CircuitBreaker(name = RESILIENCE_NAME, fallbackMethod = "publishFallback")
    @Retry(name = RESILIENCE_NAME)
    public CompletableFuture<SendResult<String, GreetingEvent>> publish(
            final GreetingEvent event) {
        Span span = tracer.nextSpan()
                .name(SPAN_NAME)
                .tag("messaging.system", "kafka")
                .tag("messaging.destination", greetingTopic)
                .tag("messaging.kafka.message_key", event.getCorrelationId())
                .tag("egds.correlationId", event.getCorrelationId())
                .start();

        LOG.info("[KAFKA] Publishing GreetingEvent"
                + " topic={} key={} traceId={}",
                greetingTopic,
                event.getCorrelationId(),
                span.context().traceId());

        CompletableFuture<SendResult<String, GreetingEvent>> future;
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            future = kafkaTemplate.send(
                    greetingTopic, event.getCorrelationId(), event);
        } finally {
            span.end();
        }

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error("[KAFKA] Publish failed"
                        + " topic={} key={} error={}",
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

    /**
     * Fallback invoked when the rate limiter, circuit breaker, or retry
     * mechanism cannot forward the publish request to the Kafka broker.
     *
     * <p>Returns a failed future so that callers can distinguish a
     * fallback response from a successful publish.
     *
     * @param event the event that could not be published
     * @param t     the cause that triggered the fallback
     * @return a {@link CompletableFuture} completed exceptionally with
     *         a wrapped runtime exception
     */
    CompletableFuture<SendResult<String, GreetingEvent>> publishFallback(
            final GreetingEvent event, final Throwable t) {
        LOG.warn("[CB-FALLBACK] kafkaPublish circuit activated."
                + " correlationId={} cause={}",
                event.getCorrelationId(), t.getMessage());
        return CompletableFuture.failedFuture(
                new RuntimeException(
                        "Kafka publish unavailable (resilience fallback): "
                        + t.getMessage(), t));
    }
}
