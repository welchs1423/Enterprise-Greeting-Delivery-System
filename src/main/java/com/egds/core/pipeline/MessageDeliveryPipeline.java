package com.egds.core.pipeline;

import com.egds.core.aspect.MessageDeliveryLoggingAspect;
import com.egds.core.dto.MessageDeliveryResult;
import com.egds.core.interfaces.IMessageOutputStrategy;
import com.egds.core.interfaces.IMessageProvider;
import com.egds.core.interfaces.IMessageValidator;
import com.egds.core.mapper.MessageMapper;
import com.egds.core.service.MessageDeliveryService;

/**
 * Top-level pipeline facade coordinating the assembly and execution of the
 * EGDS delivery service. Encapsulates all service construction and result
 * reporting logic, providing a single execution entry point for the
 * application bootstrap layer.
 */
public class MessageDeliveryPipeline {

    private final MessageDeliveryService deliveryService;

    /**
     * Constructs a {@code MessageDeliveryPipeline} by assembling the delivery service
     * from its constituent pipeline components.
     *
     * @param messageProvider  the component supplying the raw message payload
     * @param outputStrategy   the component governing output channel selection
     * @param messageMapper    the component transforming DTOs to domain entities
     * @param messageValidator the component enforcing message integrity constraints
     * @param loggingAspect    the component providing cross-cutting audit instrumentation
     */
    public MessageDeliveryPipeline(IMessageProvider messageProvider,
                                   IMessageOutputStrategy outputStrategy,
                                   MessageMapper messageMapper,
                                   IMessageValidator messageValidator,
                                   MessageDeliveryLoggingAspect loggingAspect) {
        this.deliveryService = new MessageDeliveryService(
                messageProvider, outputStrategy, messageMapper, messageValidator, loggingAspect);
    }

    /**
     * Initiates pipeline execution and handles the top-level delivery result.
     * Delegates to the underlying {@link MessageDeliveryService} and emits
     * a structured completion report to the standard error stream upon termination.
     */
    public void execute() {
        MessageDeliveryResult result = deliveryService.deliver();
        System.err.printf(
                "[EGDS] Pipeline execution complete. correlationId=%s duration=%dms success=%s%n",
                result.getCorrelationId(),
                result.getDeliveryDurationMs(),
                result.isSuccess()
        );
    }
}
