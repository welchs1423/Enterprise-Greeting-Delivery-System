package com.egds.web;

import com.egds.cqrs.query.GreetingQueryHandler;
import com.egds.cqrs.query.GreetingReadModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CQRS query-side REST endpoint for greeting delivery status lookups.
 *
 * <p>This controller operates exclusively on the read path. It serves
 * the MongoDB materialized view built by
 * {@link com.egds.cqrs.projector.GreetingProjector} without touching
 * the command side or the Kafka event log.
 *
 * <p>Clients issued a correlation ID from
 * {@code GET /api/v1/greeting} poll this endpoint to determine whether
 * the {@code GreetingRequestedEvent} has been projected and whether
 * the downstream delivery pipeline has completed.
 *
 * <p>HTTP 404 is returned when the event has not yet propagated to the
 * MongoDB read model. Callers should implement exponential back-off
 * retry until either a terminal status (DELIVERED, FAILED) or their
 * own timeout is reached.
 *
 * <p>All operations require a valid JWT bearer token carrying the
 * {@code ROLE_GREETING_ADMIN} authority.
 */
@RestController
@RequestMapping("/api/v1/greeting")
public class GreetingQueryController {

    /** CQRS query handler backed by the MongoDB materialized view. */
    private final GreetingQueryHandler queryHandler;

    /**
     * @param handler the CQRS query handler
     */
    public GreetingQueryController(final GreetingQueryHandler handler) {
        this.queryHandler = handler;
    }

    /**
     * Returns the current read-model status for the given correlation ID.
     * Requires the {@code ROLE_GREETING_ADMIN} authority.
     *
     * @param correlationId the delivery correlation identifier
     * @return HTTP 200 with the {@link GreetingReadModel} if projected,
     *         or HTTP 404 if the event has not yet reached MongoDB
     */
    @GetMapping("/status/{correlationId}")
    @PreAuthorize("hasRole('GREETING_ADMIN')")
    public ResponseEntity<GreetingReadModel> getStatus(
            @PathVariable final String correlationId) {
        return queryHandler.findByCorrelationId(correlationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
