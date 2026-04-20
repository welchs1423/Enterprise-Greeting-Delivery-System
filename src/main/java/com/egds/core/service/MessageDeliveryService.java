package com.egds.core.service;

import com.egds.core.aspect.MessageDeliveryLoggingAspect;
import com.egds.core.dto.MessageContentDto;
import com.egds.core.dto.MessageDeliveryResult;
import com.egds.core.entity.MessageEntity;
import com.egds.core.interfaces.IMessageDeliveryService;
import com.egds.core.interfaces.IMessageOutputStrategy;
import com.egds.core.interfaces.IMessageProvider;
import com.egds.core.interfaces.IMessageValidator;
import com.egds.core.mapper.MessageMapper;

/**
 * Core orchestration service implementing the EGDS message delivery lifecycle.
 * Coordinates the sequential invocation of content provision, validation,
 * mapping, aspect interception, and output strategy execution.
 * All lifecycle stage boundaries are instrumented via the injected {@link MessageDeliveryLoggingAspect}.
 */
public class MessageDeliveryService implements IMessageDeliveryService {

    private final IMessageProvider messageProvider;
    private final IMessageOutputStrategy outputStrategy;
    private final MessageMapper messageMapper;
    private final IMessageValidator messageValidator;
    private final MessageDeliveryLoggingAspect loggingAspect;

    /**
     * Constructs a {@code MessageDeliveryService} with all required pipeline collaborators.
     *
     * @param messageProvider  the provider supplying the raw message payload
     * @param outputStrategy   the strategy governing output channel delivery
     * @param messageMapper    the mapper transforming validated DTOs to domain entities
     * @param messageValidator the validator enforcing message integrity constraints
     * @param loggingAspect    the aspect component providing cross-cutting audit logging
     */
    public MessageDeliveryService(IMessageProvider messageProvider,
                                  IMessageOutputStrategy outputStrategy,
                                  MessageMapper messageMapper,
                                  IMessageValidator messageValidator,
                                  MessageDeliveryLoggingAspect loggingAspect) {
        this.messageProvider = messageProvider;
        this.outputStrategy = outputStrategy;
        this.messageMapper = messageMapper;
        this.messageValidator = messageValidator;
        this.loggingAspect = loggingAspect;
    }

    /**
     * Executes the complete message delivery lifecycle.
     * Pipeline stages: content provision -> validation -> DTO-to-entity mapping -> delivery.
     * Elapsed wall-clock time is measured across the full lifecycle and included in the result.
     *
     * @return a {@link MessageDeliveryResult} containing outcome and tracing metadata
     */
    @Override
    public MessageDeliveryResult deliver() {
        long startTime = System.currentTimeMillis();

        MessageContentDto dto = messageProvider.provideMessage();

        loggingAspect.beforeValidation(dto);
        messageValidator.validate(dto);
        loggingAspect.afterValidation(dto);

        loggingAspect.beforeMapping(dto);
        MessageEntity entity = messageMapper.toEntity(dto);
        loggingAspect.afterMapping(entity);

        loggingAspect.beforeDelivery(entity);
        outputStrategy.output(entity);
        loggingAspect.afterDelivery(entity);

        long duration = System.currentTimeMillis() - startTime;
        return MessageDeliveryResult.success(dto.getCorrelationId(), duration, entity);
    }
}
