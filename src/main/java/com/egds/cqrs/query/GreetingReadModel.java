package com.egds.cqrs.query;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing the CQRS read-model materialized view
 * for a single greeting delivery lifecycle.
 *
 * <p>Documents in the {@code greeting_read_models} collection are
 * created by {@link com.egds.cqrs.projector.GreetingProjector} when
 * a {@link com.egds.cqrs.event.GreetingRequestedEvent} is consumed from
 * the Kafka event log. The {@code correlationId} field carries a sparse
 * unique index to enforce idempotency across projections.
 */
@Document(collection = "greeting_read_models")
public class GreetingReadModel {

    /** MongoDB-assigned document identifier. */
    @Id
    private String id;

    /** Correlation ID linking this view to the originating command. */
    @Indexed(unique = true, sparse = true)
    private String correlationId;

    /** Resolved client IP address captured at event sourcing time. */
    private String requestIp;

    /** Authenticated principal name captured at event sourcing time. */
    private String principalName;

    /**
     * Projection status: PROJECTED, DELIVERED, or FAILED.
     * Updated by downstream pipeline events.
     */
    private String status;

    /** ISO-8601 timestamp of when the projection was last written. */
    private String projectedAt;

    /** AI-generated greeting text populated after pipeline delivery. */
    private String greetingText;

    /**
     * Returns the document ID.
     *
     * @return the MongoDB document ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the document ID.
     *
     * @param documentId the MongoDB document ID
     */
    public void setId(final String documentId) {
        this.id = documentId;
    }

    /**
     * Returns the correlation ID.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the correlation ID.
     *
     * @param corrId the correlation ID
     */
    public void setCorrelationId(final String corrId) {
        this.correlationId = corrId;
    }

    /**
     * Returns the request IP address.
     *
     * @return the IP address string
     */
    public String getRequestIp() {
        return requestIp;
    }

    /**
     * Sets the request IP address.
     *
     * @param ip the IP address string
     */
    public void setRequestIp(final String ip) {
        this.requestIp = ip;
    }

    /**
     * Returns the principal name.
     *
     * @return the principal name string
     */
    public String getPrincipalName() {
        return principalName;
    }

    /**
     * Sets the principal name.
     *
     * @param principal the principal name
     */
    public void setPrincipalName(final String principal) {
        this.principalName = principal;
    }

    /**
     * Returns the projection status.
     *
     * @return the status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the projection status.
     *
     * @param projectionStatus the status string
     */
    public void setStatus(final String projectionStatus) {
        this.status = projectionStatus;
    }

    /**
     * Returns the ISO-8601 projection timestamp.
     *
     * @return the projectedAt timestamp string
     */
    public String getProjectedAt() {
        return projectedAt;
    }

    /**
     * Sets the ISO-8601 projection timestamp.
     *
     * @param timestamp the projection timestamp
     */
    public void setProjectedAt(final String timestamp) {
        this.projectedAt = timestamp;
    }

    /**
     * Returns the AI-generated greeting text.
     *
     * @return the greeting text string, or null if not yet delivered
     */
    public String getGreetingText() {
        return greetingText;
    }

    /**
     * Sets the AI-generated greeting text.
     *
     * @param text the greeting text
     */
    public void setGreetingText(final String text) {
        this.greetingText = text;
    }
}
