package com.egds.web.dto;

/**
 * Response body for the asynchronous greeting delivery endpoint
 * ({@code GET /api/v1/greeting}).
 * The correlation identifier can be used to trace the delivery event
 * through the Kafka topic and the JPA audit log.
 */
public class GreetingResponse {

    private final String correlationId;
    private final String status;
    private final String message;

    public GreetingResponse(String correlationId) {
        this.correlationId = correlationId;
        this.status = "ACCEPTED";
        this.message = "Greeting delivery event published. Pipeline execution is asynchronous.";
    }

    public String getCorrelationId() { return correlationId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}
