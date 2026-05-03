package com.egds.cqrs.query;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for {@link GreetingReadModel} documents.
 *
 * <p>All query operations on the CQRS read path are routed through this
 * repository. The underlying MongoDB collection is
 * {@code greeting_read_models}. No write operations may be called from
 * query handlers; inserts and updates are reserved for
 * {@link com.egds.cqrs.projector.GreetingProjector}.
 */
public interface GreetingReadModelRepository
        extends MongoRepository<GreetingReadModel, String> {

    /**
     * Finds the read model for the given correlation identifier.
     *
     * @param correlationId the correlation ID to look up
     * @return an {@link Optional} containing the document, or empty
     */
    Optional<GreetingReadModel> findByCorrelationId(
            String correlationId);

    /**
     * Returns all read models with the specified projection status.
     *
     * @param status the status value to filter by (e.g., PROJECTED,
     *               DELIVERED, FAILED)
     * @return list of matching documents
     */
    List<GreetingReadModel> findByStatus(String status);
}
