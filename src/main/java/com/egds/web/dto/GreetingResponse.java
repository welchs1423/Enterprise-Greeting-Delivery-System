package com.egds.web.dto;

/**
 * Response body for the asynchronous greeting delivery endpoint
 * ({@code GET /api/v1/greeting}). The correlation identifier can be used
 * to trace the delivery event through the Kafka topic and JPA audit log.
 */
public final class GreetingResponse {

    /** Unique correlation identifier for tracing this delivery event. */
    private final String correlationId;

    /** Acceptance status string. */
    private final String status;

    /** Human-readable message describing the async delivery initiation. */
    private final String message;

    /**
     * Constructs a GreetingResponse for the given correlation ID.
     *
     * @param corrId the unique correlation identifier
     */
    public GreetingResponse(final String corrId) {
        this.correlationId = corrId;
        this.status = "ACCEPTED";
        this.message =
                "Greeting delivery event published."
                + " Pipeline execution is asynchronous.";
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
     * Returns the acceptance status.
     *
     * @return the status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the human-readable acceptance message.
     *
     * @return the message string
     */
    public String getMessage() {
        return message;
    }
}
