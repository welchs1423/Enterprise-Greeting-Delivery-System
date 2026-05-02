package com.egds.messaging;

import com.egds.core.dto.MessageDeliveryResult;
import com.egds.core.entity.GreetingAuditLog;
import com.egds.core.pipeline.MessageDeliveryPipeline;
import com.egds.core.service.AuditLogService;
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
 * <p>Configured for at-least-once delivery semantics.
 */
@Component
public class GreetingEventConsumer {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingEventConsumer.class);

    /** Pipeline facade for driving message delivery. */
    private final MessageDeliveryPipeline messageDeliveryPipeline;

    /** Service for persisting delivery audit records. */
    private final AuditLogService auditLogService;

    /**
     * @param pipeline   the delivery pipeline facade
     * @param logService the audit log persistence service
     */
    public GreetingEventConsumer(
            final MessageDeliveryPipeline pipeline,
            final AuditLogService logService) {
        this.messageDeliveryPipeline = pipeline;
        this.auditLogService = logService;
    }

    /**
     * Consumes a {@link GreetingEvent} and drives the EGDS delivery
     * pipeline. The greeting string is assembled via the cache layer
     * inside the pipeline and delivered through the output strategy.
     *
     * @param event the greeting event received from the Kafka topic
     */
    @KafkaListener(
            topics = "${egds.kafka.topic.greeting}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload final GreetingEvent event) {
        LOG.info("[KAFKA] Received GreetingEvent"
                + " correlationId={} ip={} principal={}",
                event.getCorrelationId(),
                event.getRequestIp(),
                event.getPrincipalName());

        MessageDeliveryResult result = messageDeliveryPipeline.execute();

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

        LOG.info("[KAFKA] Processing complete"
                + " correlationId={} status={} durationMs={}",
                event.getCorrelationId(),
                auditLog.getDeliveryStatus(),
                auditLog.getDurationMs());
    }
}
