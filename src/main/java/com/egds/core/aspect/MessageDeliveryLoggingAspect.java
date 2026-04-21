package com.egds.core.aspect;

import com.egds.core.dto.MessageContentDto;
import com.egds.core.entity.MessageEntity;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Cross-cutting concern component providing structured lifecycle logging for the
 * EGDS message delivery pipeline. Simulates the behavior of an AOP interceptor,
 * capturing pre-invocation and post-invocation audit events for each pipeline stage.
 *
 * In a Spring-managed context, this class would be annotated with {@code @Aspect}
 * and individual methods would carry {@code @Around} advice targeting the
 * {@code MessageDeliveryService} join points.
 */
@Component
public class MessageDeliveryLoggingAspect {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                             .withZone(ZoneId.of("UTC"));

    /**
     * Records a pre-invocation audit log entry for the message validation stage.
     *
     * @param dto the message payload entering the validation boundary
     */
    public void beforeValidation(MessageContentDto dto) {
        log("AUDIT", "PRE_VALIDATION",
                "correlationId=" + dto.getCorrelationId() + " priority=" + dto.getPriority());
    }

    /**
     * Records a post-invocation audit log entry following successful message validation.
     *
     * @param dto the message payload that passed validation
     */
    public void afterValidation(MessageContentDto dto) {
        log("AUDIT", "POST_VALIDATION",
                "correlationId=" + dto.getCorrelationId() + " status=PASSED");
    }

    /**
     * Records a pre-invocation audit log entry for the message mapping stage.
     *
     * @param dto the message payload entering the mapping boundary
     */
    public void beforeMapping(MessageContentDto dto) {
        log("AUDIT", "PRE_MAPPING",
                "correlationId=" + dto.getCorrelationId());
    }

    /**
     * Records a post-invocation audit log entry following successful DTO-to-entity mapping.
     *
     * @param entity the entity produced by the mapping operation
     */
    public void afterMapping(MessageEntity entity) {
        log("AUDIT", "POST_MAPPING",
                "correlationId=" + entity.getCorrelationId() + " entityId=" + entity.getEntityId());
    }

    /**
     * Records a pre-invocation audit log entry for the output delivery stage.
     *
     * @param entity the entity entering the output strategy boundary
     */
    public void beforeDelivery(MessageEntity entity) {
        log("AUDIT", "PRE_DELIVERY",
                "correlationId=" + entity.getCorrelationId() + " entityId=" + entity.getEntityId());
    }

    /**
     * Records a post-invocation audit log entry following output strategy completion.
     *
     * @param entity the entity that has exited the output strategy boundary
     */
    public void afterDelivery(MessageEntity entity) {
        log("AUDIT", "POST_DELIVERY",
                "correlationId=" + entity.getCorrelationId()
                        + " entityId=" + entity.getEntityId()
                        + " status=" + entity.getDeliveryStatus());
    }

    /**
     * Emits a structured log entry to the standard error stream.
     * Standard error is used to maintain separation between audit output
     * and application payload output on the console output stream.
     *
     * @param level     the log severity level (e.g., "AUDIT", "ERROR")
     * @param eventName the pipeline event identifier
     * @param detail    supplementary key-value pairs describing the event
     */
    private void log(String level, String eventName, String detail) {
        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        System.err.printf("[%s] [%s] [EGDS] event=%s %s%n", timestamp, level, eventName, detail);
    }
}
