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
 * Kafka consumer component that receives {@link GreetingEvent} instances from the EGDS
 * greeting event topic and orchestrates the full delivery pipeline execution.
 *
 * <p>For each consumed event, the following steps are performed sequentially on the
 * Kafka consumer thread:
 * <ol>
 *   <li>Invoke the delivery pipeline (cache-backed content provision, validation,
 *       mapping, console output).</li>
 *   <li>Persist a {@link GreetingAuditLog} record capturing the request IP,
 *       executing thread name, principal, and delivery outcome.</li>
 * </ol>
 *
 * <p>The consumer group is configured for at-least-once delivery semantics.
 * Idempotency considerations must be applied at the audit log layer if
 * exactly-once guarantees are required.
 */
@Component
public class GreetingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(GreetingEventConsumer.class);

    private final MessageDeliveryPipeline messageDeliveryPipeline;
    private final AuditLogService auditLogService;

    public GreetingEventConsumer(MessageDeliveryPipeline messageDeliveryPipeline,
                                 AuditLogService auditLogService) {
        this.messageDeliveryPipeline = messageDeliveryPipeline;
        this.auditLogService = auditLogService;
    }

    /**
     * Consumes a {@link GreetingEvent} and drives the complete EGDS delivery pipeline.
     * The string "Hello, World!" is not written directly; it is assembled via the
     * cache layer inside the pipeline and delivered through the output strategy.
     *
     * @param event the greeting event received from the Kafka topic
     */
    @KafkaListener(
            topics = "${egds.kafka.topic.greeting}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload GreetingEvent event) {
        log.info("[KAFKA] Received GreetingEvent correlationId={} ip={} principal={}",
                event.getCorrelationId(), event.getRequestIp(), event.getPrincipalName());

        MessageDeliveryResult result = messageDeliveryPipeline.execute();

        GreetingAuditLog auditLog = new GreetingAuditLog.Builder()
                .correlationId(event.getCorrelationId())
                .requestIp(event.getRequestIp())
                .threadName(Thread.currentThread().getName())
                .principalName(event.getPrincipalName())
                .deliveryStatus(result.isSuccess() ? "DELIVERED" : "FAILED")
                .durationMs(result.getDeliveryDurationMs())
                .build();

        auditLogService.record(auditLog);

        log.info("[KAFKA] Processing complete correlationId={} status={} durationMs={}",
                event.getCorrelationId(),
                auditLog.getDeliveryStatus(),
                auditLog.getDurationMs());
    }
}
