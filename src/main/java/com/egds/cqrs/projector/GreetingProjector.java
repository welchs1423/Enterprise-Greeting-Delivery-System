package com.egds.cqrs.projector;

import com.egds.cqrs.event.GreetingRequestedEvent;
import com.egds.cqrs.query.GreetingReadModel;
import com.egds.cqrs.query.GreetingReadModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Event projector that subscribes to the {@code egds.greeting.requested}
 * Kafka topic and builds a MongoDB materialized view for the CQRS read
 * path.
 *
 * <p>For each {@link GreetingRequestedEvent} received, the projector
 * upserts a {@link GreetingReadModel} document in MongoDB with status
 * {@code PROJECTED}.  Subsequent pipeline events (DELIVERED, FAILED)
 * update the same document via the correlation ID as the natural key.
 *
 * <p>Projection is idempotent: a duplicate event for an already-projected
 * correlation ID overwrites the document with the same data, producing no
 * observable side-effect on the read model.
 */
@Component
public class GreetingProjector {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingProjector.class);

    /** MongoDB read-model repository for upsert operations. */
    private final GreetingReadModelRepository readModelRepository;

    /**
     * @param repository the MongoDB read-model repository
     */
    public GreetingProjector(
            final GreetingReadModelRepository repository) {
        this.readModelRepository = repository;
    }

    /**
     * Consumes a {@link GreetingRequestedEvent} from the event log and
     * projects it into a MongoDB {@link GreetingReadModel} document.
     *
     * @param event the domain event to project; must not be null
     */
    @KafkaListener(
            topics = "${egds.kafka.topic.greeting-requested}",
            groupId = "${egds.kafka.cqrs.consumer-group:"
                    + "egds-projector-group}",
            containerFactory = "kafkaListenerContainerFactory")
    public void project(final GreetingRequestedEvent event) {
        LOG.info("[PROJECTOR] projecting correlationId={}",
                event.getCorrelationId());

        GreetingReadModel readModel = readModelRepository
                .findByCorrelationId(event.getCorrelationId())
                .orElse(new GreetingReadModel());

        readModel.setCorrelationId(event.getCorrelationId());
        readModel.setRequestIp(event.getRequestIp());
        readModel.setPrincipalName(event.getPrincipalName());
        readModel.setStatus("PROJECTED");
        readModel.setProjectedAt(
                Instant.ofEpochMilli(event.getRequestedAtEpochMs())
                        .toString());

        readModelRepository.save(readModel);

        LOG.info("[PROJECTOR] read model upserted correlationId={}",
                event.getCorrelationId());
    }
}
