package com.egds.cqrs.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Query handler for the CQRS read path.
 *
 * <p>Serves read-model lookups exclusively from the MongoDB materialized
 * view populated by {@link com.egds.cqrs.projector.GreetingProjector}.
 * This component must not invoke any command handler or publish any
 * Kafka event.
 *
 * <p>Callers receive an {@link Optional} allowing them to distinguish
 * between a delivery that has been projected (event received) and one
 * that has not yet propagated to the read model.
 */
@Component
public class GreetingQueryHandler {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingQueryHandler.class);

    /** MongoDB read-model repository. */
    private final GreetingReadModelRepository readModelRepository;

    /**
     * @param repository the MongoDB read-model repository
     */
    public GreetingQueryHandler(
            final GreetingReadModelRepository repository) {
        this.readModelRepository = repository;
    }

    /**
     * Retrieves the read model for the given correlation ID.
     *
     * @param correlationId the delivery correlation identifier
     * @return an {@link Optional} containing the read model if projected,
     *         or empty if the event has not yet been processed
     */
    public Optional<GreetingReadModel> findByCorrelationId(
            final String correlationId) {
        LOG.debug("[CQRS-QUERY] findByCorrelationId={}",
                correlationId);
        return readModelRepository.findByCorrelationId(correlationId);
    }
}
