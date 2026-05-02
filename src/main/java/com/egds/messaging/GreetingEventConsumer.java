package com.egds.messaging;

import com.egds.core.dto.MessageDeliveryResult;
import com.egds.core.entity.GreetingAuditLog;
import com.egds.core.pipeline.MessageDeliveryPipeline;
import com.egds.core.service.AuditLogService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that receives {@link GreetingEvent} instances from the
 * EGDS greeting event topic and orchestrates the full delivery pipeline.
 *
 * <p>For each consumed event, the following steps are performed
 * sequentially on the Kafka consumer thread:
 * <ol>
 *   <li>Invoke the delivery pipeline (cache-backed content provision,
 *       validation, mapping, console output).</li>
 *   <li>Persist a {@link GreetingAuditLog} record capturing the request
 *       IP, thread name, principal, and delivery outcome.</li>
 * </ol>
 *
 * <p>Each consumption cycle is wrapped in an OpenTelemetry
 * {@link Span} to propagate trace context from the Kafka message through
 * the full pipeline and audit persistence. The span carries the
 * {@code correlationId}, requesting principal, and delivery outcome
 * as tags.
 *
 * <p>The {@code consume} method is guarded by a Resilience4j Circuit
 * Breaker ({@code consumerPipeline}). When the breaker opens (after
 * 60% failure rate over 10 calls), the event is acknowledged to prevent
 * consumer lag accumulation and a {@code FAILED} audit record is written
 * by the fallback handler.
 *
 * <p>Configured for at-least-once delivery semantics.
 */
@Component
public class GreetingEventConsumer {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingEventConsumer.class);

    /** Resilience4j Circuit Breaker instance name. */
    private static final String CB_NAME = "consumerPipeline";

    /** OTel span name for consumer pipeline operations. */
    private static final String SPAN_NAME = "egds.consumer-pipeline";

    /** Pipeline facade for driving message delivery. */
    private final MessageDeliveryPipeline messageDeliveryPipeline;

    /** Service for persisting delivery audit records. */
    private final AuditLogService auditLogService;

    /** Tracer used to create and manage consumer spans. */
    private final Tracer tracer;

    /**
     * @param pipeline   the delivery pipeline facade
     * @param logService the audit log persistence service
     * @param otelTracer the Micrometer Tracing tracer
     */
    public GreetingEventConsumer(
            final MessageDeliveryPipeline pipeline,
            final AuditLogService logService,
            final Tracer otelTracer) {
        this.messageDeliveryPipeline = pipeline;
        this.auditLogService = logService;
        this.tracer = otelTracer;
    }

    /**
     * Consumes a {@link GreetingEvent} and drives the EGDS delivery
     * pipeline. The greeting string is assembled via the cache layer
     * inside the pipeline and delivered through the output strategy.
     *
     * <p>An OTel span is opened for the full consumption cycle. The
     * span's trace identifier is propagated to all child spans created
     * within the pipeline and audit service.
     *
     * <p>Guarded by {@code @CircuitBreaker(consumerPipeline)}. When the
     * breaker activates, {@link #consumeFallback} is called instead.
     *
     * @param event the greeting event received from the Kafka topic
     */
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "consumeFallback")
    @KafkaListener(
            topics = "${egds.kafka.topic.greeting}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload final GreetingEvent event) {
        Span span = tracer.nextSpan()
                .name(SPAN_NAME)
                .tag("messaging.system", "kafka")
                .tag("egds.correlationId", event.getCorrelationId())
                .tag("egds.principal", event.getPrincipalName())
                .tag("egds.requestIp", event.getRequestIp())
                .start();

        LOG.info("[KAFKA] Received GreetingEvent"
                + " correlationId={} ip={} principal={} traceId={}",
                event.getCorrelationId(),
                event.getRequestIp(),
                event.getPrincipalName(),
                span.context().traceId());

        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            MessageDeliveryResult result =
                    messageDeliveryPipeline.execute();

            GreetingAuditLog auditLog = new GreetingAuditLog.Builder()
                    .correlationId(event.getCorrelationId())
                    .requestIp(event.getRequestIp())
                    .threadName(Thread.currentThread().getName())
                    .principalName(event.getPrincipalName())
                    .deliveryStatus(
                            result.isSuccess() ? "DELIVERED" : "FAILED")
                    .durationMs(result.getDeliveryDurationMs())
                    .build();

            auditLogService.record(auditLog);

            span.tag("egds.deliveryStatus", auditLog.getDeliveryStatus());
            LOG.info("[KAFKA] Processing complete"
                    + " correlationId={} status={} durationMs={}",
                    event.getCorrelationId(),
                    auditLog.getDeliveryStatus(),
                    auditLog.getDurationMs());
        } finally {
            span.end();
        }
    }

    /**
     * Fallback invoked when the {@code consumerPipeline} circuit breaker
     * is open or the pipeline throws an unrecoverable exception.
     *
     * <p>Acknowledges the Kafka offset (by returning normally) to
     * prevent consumer lag accumulation. Persists a {@code FAILED}
     * audit record with zero duration to preserve the audit trail.
     *
     * @param event the event that could not be processed
     * @param t     the cause that triggered the fallback
     */
    void consumeFallback(
            final GreetingEvent event, final Throwable t) {
        LOG.warn("[CB-FALLBACK] consumerPipeline circuit activated."
                + " correlationId={} cause={}",
                event.getCorrelationId(), t.getMessage());

        GreetingAuditLog fallbackLog = new GreetingAuditLog.Builder()
                .correlationId(event.getCorrelationId())
                .requestIp(event.getRequestIp())
                .threadName(Thread.currentThread().getName())
                .principalName(event.getPrincipalName())
                .deliveryStatus("FAILED")
                .durationMs(0L)
                .build();
        auditLogService.record(fallbackLog);
    }
}
