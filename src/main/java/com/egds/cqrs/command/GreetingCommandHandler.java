package com.egds.cqrs.command;

import com.egds.cqrs.event.GreetingRequestedEvent;
import com.egds.messaging.GreetingEvent;
import com.egds.messaging.GreetingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Handles {@link DeliverGreetingCommand} on the CQRS write path.
 *
 * <p>On receipt of a command this handler performs two publish operations:
 * <ol>
 *   <li>Publishes a {@link GreetingRequestedEvent} to the dedicated CQRS
 *       event-sourcing topic ({@code egds.greeting.requested}).  This
 *       event constitutes the authoritative write to the event log and is
 *       consumed by {@link com.egds.cqrs.projector.GreetingProjector} to
 *       build the MongoDB materialized view.</li>
 *   <li>Publishes a {@link GreetingEvent} to the legacy delivery topic
 *       ({@code egds.greeting.events}) to preserve compatibility with
 *       the existing {@link com.egds.messaging.GreetingEventConsumer}
 *       pipeline.</li>
 * </ol>
 *
 * <p>Both publish calls are fire-and-forget. The handler does not wait
 * for broker acknowledgement, maintaining the HTTP layer's
 * non-blocking 202 contract.
 */
@Component
public class GreetingCommandHandler {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingCommandHandler.class);

    /** CQRS event-sourcing topic for GreetingRequestedEvent records. */
    @Value("${egds.kafka.topic.greeting-requested}")
    private String greetingRequestedTopic;

    /** KafkaTemplate for publishing GreetingRequestedEvent. */
    private final KafkaTemplate<String, GreetingRequestedEvent>
            requestedEventTemplate;

    /** Legacy event publisher for the existing delivery pipeline. */
    private final GreetingEventPublisher greetingEventPublisher;

    /**
     * @param eventTemplate    KafkaTemplate for GreetingRequestedEvent
     * @param eventPublisher   legacy delivery event publisher
     */
    public GreetingCommandHandler(
            final KafkaTemplate<String, GreetingRequestedEvent>
                    eventTemplate,
            final GreetingEventPublisher eventPublisher) {
        this.requestedEventTemplate = eventTemplate;
        this.greetingEventPublisher = eventPublisher;
    }

    /**
     * Processes the supplied command by emitting domain events to Kafka.
     *
     * @param command the command to handle; must not be null
     */
    public void handle(final DeliverGreetingCommand command) {
        LOG.info("[CQRS-CMD] handling DeliverGreetingCommand"
                + " correlationId={}", command.getCorrelationId());

        GreetingRequestedEvent requestedEvent = new GreetingRequestedEvent(
                command.getCorrelationId(),
                command.getRequestIp(),
                command.getPrincipalName(),
                Instant.now().toEpochMilli());

        requestedEventTemplate.send(
                greetingRequestedTopic,
                command.getCorrelationId(),
                requestedEvent);

        greetingEventPublisher.publish(
                new GreetingEvent(
                        command.getCorrelationId(),
                        command.getRequestIp(),
                        command.getPrincipalName()));

        LOG.info("[CQRS-CMD] events published correlationId={}",
                command.getCorrelationId());
    }
}
