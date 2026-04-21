package com.egds.core.pipeline;

import com.egds.core.dto.MessageDeliveryResult;
import com.egds.core.service.MessageDeliveryService;
import org.springframework.stereotype.Component;

/**
 * Top-level pipeline facade coordinating the execution of the EGDS delivery service.
 * Encapsulates result reporting and provides a single execution entry point for callers
 * such as the Kafka consumer.
 *
 * <p>In the Spring-managed context, all pipeline dependencies are wired by the IoC
 * container via constructor injection. The factory-based wiring present in v1.0 is
 * superseded by Spring DI; factory classes are retained for non-Spring environments.
 */
@Component
public class MessageDeliveryPipeline {

    private final MessageDeliveryService deliveryService;

    /**
     * Constructs a {@code MessageDeliveryPipeline} with a Spring-managed
     * {@link MessageDeliveryService}.
     *
     * @param deliveryService the delivery service that orchestrates the full pipeline lifecycle
     */
    public MessageDeliveryPipeline(MessageDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /**
     * Initiates pipeline execution and returns the delivery result.
     * Delegates to the underlying {@link MessageDeliveryService} and emits
     * a structured completion report to the standard error stream.
     *
     * @return the {@link MessageDeliveryResult} produced by the delivery service
     */
    public MessageDeliveryResult execute() {
        MessageDeliveryResult result = deliveryService.deliver();
        System.err.printf(
                "[EGDS] Pipeline execution complete. correlationId=%s duration=%dms success=%s%n",
                result.getCorrelationId(),
                result.getDeliveryDurationMs(),
                result.isSuccess()
        );
        return result;
    }
}
