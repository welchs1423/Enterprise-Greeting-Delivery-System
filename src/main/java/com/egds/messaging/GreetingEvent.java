package com.egds.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event payload published to the EGDS greeting event Kafka topic.
 * Carries the contextual metadata required for pipeline execution and audit log persistence.
 *
 * <p>Serialized as JSON for Kafka transport via Spring Kafka's {@code JsonSerializer}.
 * A no-argument constructor is required for Jackson deserialization on the consumer side.
 * Unknown fields are silently ignored to support forward-compatible schema evolution.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GreetingEvent {

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("requestIp")
    private String requestIp;

    @JsonProperty("principalName")
    private String principalName;

    /** Epoch millisecond timestamp at which the event was created by the publisher. */
    @JsonProperty("issuedAt")
    private long issuedAt;

    /** Required by Jackson for JSON deserialization. Not for direct use. */
    public GreetingEvent() {
    }

    /**
     * Constructs a {@code GreetingEvent} with the originating request context.
     * {@code issuedAt} is set to the current system time at construction.
     *
     * @param correlationId unique identifier for tracing this delivery cycle
     * @param requestIp     IP address of the client that initiated the HTTP request
     * @param principalName authenticated username extracted from the JWT token
     */
    public GreetingEvent(String correlationId, String requestIp, String principalName) {
        this.correlationId = correlationId;
        this.requestIp = requestIp;
        this.principalName = principalName;
        this.issuedAt = System.currentTimeMillis();
    }

    public String getCorrelationId() { return correlationId; }
    public String getRequestIp() { return requestIp; }
    public String getPrincipalName() { return principalName; }
    public long getIssuedAt() { return issuedAt; }

    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public void setRequestIp(String requestIp) { this.requestIp = requestIp; }
    public void setPrincipalName(String principalName) { this.principalName = principalName; }
    public void setIssuedAt(long issuedAt) { this.issuedAt = issuedAt; }

    @Override
    public String toString() {
        return "GreetingEvent{"
                + "correlationId='" + correlationId + '\''
                + ", requestIp='" + requestIp + '\''
                + ", principalName='" + principalName + '\''
                + ", issuedAt=" + issuedAt
                + '}';
    }
}
