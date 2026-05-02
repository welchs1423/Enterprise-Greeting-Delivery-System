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
import org.springframework.stereotype.Service;

/**
 * Core orchestration service implementing the EGDS message delivery
 * lifecycle. Coordinates the sequential invocation of content provision,
 * validation, mapping, aspect interception, and output strategy execution.
 *
 * <p>All lifecycle stage boundaries are instrumented via the injected
 * {@link MessageDeliveryLoggingAspect}. In the Spring-managed context,
 * all collaborators are injected via constructor by the IoC container.
 */
@Service
public class MessageDeliveryService implements IMessageDeliveryService {

    /** Provider supplying the raw message payload. */
    private final IMessageProvider messageProvider;

    /** Strategy governing output channel delivery. */
    private final IMessageOutputStrategy outputStrategy;

    /** Mapper transforming validated DTOs to domain entities. */
    private final MessageMapper messageMapper;

    /** Validator enforcing message integrity constraints. */
    private final IMessageValidator messageValidator;

    /** Aspect component providing cross-cutting audit logging. */
    private final MessageDeliveryLoggingAspect loggingAspect;

    /**
     * Constructs a {@code MessageDeliveryService} with all required
     * pipeline collaborators. All parameters are injected by Spring IoC.
     *
     * @param provider  the provider supplying the raw message payload
     * @param strategy  the strategy governing output channel delivery
     * @param mapper    the mapper transforming validated DTOs to entities
     * @param validator the validator enforcing message integrity
     * @param aspect    the aspect providing cross-cutting audit logging
     */
    public MessageDeliveryService(
            final IMessageProvider provider,
            final IMessageOutputStrategy strategy,
            final MessageMapper mapper,
            final IMessageValidator validator,
            final MessageDeliveryLoggingAspect aspect) {
        this.messageProvider = provider;
        this.outputStrategy = strategy;
        this.messageMapper = mapper;
        this.messageValidator = validator;
        this.loggingAspect = aspect;
    }

    /**
     * Executes the complete message delivery lifecycle.
     * Pipeline stages: content provision, validation, DTO-to-entity
     * mapping, delivery. Elapsed wall-clock time is measured across the
     * full lifecycle and included in the result.
     *
     * @return a {@link MessageDeliveryResult} with outcome and trace data
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
        return MessageDeliveryResult.success(
                dto.getCorrelationId(), duration, entity);
    }
}
