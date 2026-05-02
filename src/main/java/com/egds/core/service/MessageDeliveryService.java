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
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Service;

/**
 * Core orchestration service implementing the EGDS message delivery
 * lifecycle. Coordinates the sequential invocation of content provision,
 * validation, mapping, aspect interception, and output strategy execution.
 *
 * <p>Each lifecycle stage (provision, validate, map, deliver) is wrapped
 * in a dedicated OpenTelemetry child {@link Span} so that the full
 * causal chain of a single "Hello, World!" delivery is visible in any
 * distributed trace backend. All spans share the trace identifier
 * established by the enclosing consumer span.
 *
 * <p>In the Spring-managed context, all collaborators are injected via
 * constructor by the IoC container.
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

    /** Tracer for creating per-stage child spans. */
    private final Tracer tracer;

    /**
     * Constructs a {@code MessageDeliveryService} with all required
     * pipeline collaborators. All parameters are injected by Spring IoC.
     *
     * @param provider  the provider supplying the raw message payload
     * @param strategy  the strategy governing output channel delivery
     * @param mapper    the mapper transforming validated DTOs to entities
     * @param validator the validator enforcing message integrity
     * @param aspect    the aspect providing cross-cutting audit logging
     * @param otelTracer the Micrometer Tracing tracer for child spans
     */
    public MessageDeliveryService(
            final IMessageProvider provider,
            final IMessageOutputStrategy strategy,
            final MessageMapper mapper,
            final IMessageValidator validator,
            final MessageDeliveryLoggingAspect aspect,
            final Tracer otelTracer) {
        this.messageProvider = provider;
        this.outputStrategy = strategy;
        this.messageMapper = mapper;
        this.messageValidator = validator;
        this.loggingAspect = aspect;
        this.tracer = otelTracer;
    }

    /**
     * Executes the complete message delivery lifecycle.
     * Pipeline stages: content provision, validation, DTO-to-entity
     * mapping, delivery. Each stage is wrapped in a dedicated child
     * {@link Span} tagged with the correlation identifier and stage name.
     * Elapsed wall-clock time is measured across the full lifecycle and
     * included in the result.
     *
     * @return a {@link MessageDeliveryResult} with outcome and trace data
     */
    @Override
    public MessageDeliveryResult deliver() {
        long startTime = System.currentTimeMillis();

        // Stage 1: content provision
        MessageContentDto dto = spanWrapProvision();

        // Stage 2: validation
        spanWrapValidation(dto);

        // Stage 3: DTO-to-entity mapping
        MessageEntity entity = spanWrapMapping(dto);

        // Stage 4: output strategy delivery
        spanWrapDelivery(entity);

        long duration = System.currentTimeMillis() - startTime;
        return MessageDeliveryResult.success(
                dto.getCorrelationId(), duration, entity);
    }

    /**
     * Runs the content provision stage inside a traced child span.
     *
     * @return the populated {@link MessageContentDto}
     */
    private MessageContentDto spanWrapProvision() {
        Span span = tracer.nextSpan()
                .name("egds.stage.provision")
                .tag("egds.stage", "provision")
                .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            MessageContentDto dto = messageProvider.provideMessage();
            span.tag("egds.correlationId", dto.getCorrelationId());
            return dto;
        } finally {
            span.end();
        }
    }

    /**
     * Runs the validation stage inside a traced child span.
     *
     * @param dto the DTO to validate
     */
    private void spanWrapValidation(final MessageContentDto dto) {
        Span span = tracer.nextSpan()
                .name("egds.stage.validate")
                .tag("egds.stage", "validate")
                .tag("egds.correlationId", dto.getCorrelationId())
                .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            loggingAspect.beforeValidation(dto);
            messageValidator.validate(dto);
            loggingAspect.afterValidation(dto);
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Runs the DTO-to-entity mapping stage inside a traced child span.
     *
     * @param dto the validated DTO to map
     * @return the resulting {@link MessageEntity}
     */
    private MessageEntity spanWrapMapping(final MessageContentDto dto) {
        Span span = tracer.nextSpan()
                .name("egds.stage.map")
                .tag("egds.stage", "map")
                .tag("egds.correlationId", dto.getCorrelationId())
                .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            loggingAspect.beforeMapping(dto);
            MessageEntity entity = messageMapper.toEntity(dto);
            loggingAspect.afterMapping(entity);
            return entity;
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Runs the output delivery stage inside a traced child span.
     * The delivery span is a parent to the child span created inside
     * {@link com.egds.core.strategy.ConsoleOutputStrategy}.
     *
     * @param entity the entity to deliver
     */
    private void spanWrapDelivery(final MessageEntity entity) {
        Span span = tracer.nextSpan()
                .name("egds.stage.deliver")
                .tag("egds.stage", "deliver")
                .tag("egds.correlationId", entity.getCorrelationId())
                .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            loggingAspect.beforeDelivery(entity);
            outputStrategy.output(entity);
            loggingAspect.afterDelivery(entity);
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
