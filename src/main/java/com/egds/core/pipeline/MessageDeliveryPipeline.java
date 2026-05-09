package com.egds.core.pipeline;

import com.egds.agile.ScrumMasterDaemon;
import com.egds.core.dto.MessageDeliveryResult;
import com.egds.core.service.MessageDeliveryService;
import org.springframework.stereotype.Component;

/**
 * Top-level pipeline facade coordinating the execution of the EGDS
 * delivery service. Encapsulates result reporting and provides a single
 * execution entry point for callers such as the Kafka consumer.
 *
 * <p>In the Spring-managed context, all pipeline dependencies are wired
 * by the IoC container via constructor injection. The factory-based
 * wiring present in v1.0 is superseded by Spring DI; factory classes
 * are retained for non-Spring environments.
 *
 * <p>Each execution cycle is preceded by a mandatory daily standup
 * ceremony enforced by {@link ScrumMasterDaemon}.
 */
@Component
public class MessageDeliveryPipeline {

    /** The delivery service orchestrating the full pipeline lifecycle. */
    private final MessageDeliveryService deliveryService;

    /** Daemon that enforces daily standup ceremonies before delivery. */
    private final ScrumMasterDaemon scrumMasterDaemon;

    /**
     * Constructs a {@code MessageDeliveryPipeline} with all collaborators.
     *
     * @param service     the delivery service for the full pipeline lifecycle
     * @param scrumMaster the daemon enforcing daily standup ceremonies
     */
    public MessageDeliveryPipeline(
            final MessageDeliveryService service,
            final ScrumMasterDaemon scrumMaster) {
        this.deliveryService = service;
        this.scrumMasterDaemon = scrumMaster;
    }

    /**
     * Initiates pipeline execution and returns the delivery result.
     * A mandatory standup ceremony is conducted before delegating to the
     * underlying {@link MessageDeliveryService}. Emits a structured
     * completion report to the standard error stream.
     *
     * @return the {@link MessageDeliveryResult} produced by the service
     */
    public MessageDeliveryResult execute() {
        scrumMasterDaemon.conductDailyStandup();
        MessageDeliveryResult result = deliveryService.deliver();
        System.err.printf(
                "[EGDS] Pipeline execution complete."
                + " correlationId=%s duration=%dms success=%s%n",
                result.getCorrelationId(),
                result.getDeliveryDurationMs(),
                result.isSuccess()
        );
        return result;
    }
}
