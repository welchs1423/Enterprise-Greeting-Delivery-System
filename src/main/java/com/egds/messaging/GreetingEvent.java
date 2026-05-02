package com.egds.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event payload published to the EGDS greeting event Kafka topic.
 * Carries the contextual metadata required for pipeline execution
 * and audit log persistence.
 *
 * <p>Serialized as JSON via Spring Kafka's {@code JsonSerializer}.
 * A no-argument constructor is required for Jackson deserialization.
 * Unknown fields are silently ignored to support schema evolution.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GreetingEvent {

    /** Unique identifier for tracing this delivery cycle. */
    @JsonProperty("correlationId")
    private String correlationId;

    /** IP address of the client that initiated the HTTP request. */
    @JsonProperty("requestIp")
    private String requestIp;

    /** Authenticated username extracted from the JWT token. */
    @JsonProperty("principalName")
    private String principalName;

    /** Epoch millisecond timestamp at which the event was created. */
    @JsonProperty("issuedAt")
    private long issuedAt;

    /** Required by Jackson for JSON deserialization. Not for direct use. */
    public GreetingEvent() {
    }

    /**
     * Constructs a {@code GreetingEvent} with the originating request
     * context. {@code issuedAt} is set to the current system time.
     *
     * @param corrId    unique identifier for tracing this delivery cycle
     * @param ip        IP address of the client
     * @param principal authenticated username from the JWT token
     */
    public GreetingEvent(
            final String corrId,
            final String ip,
            final String principal) {
        this.correlationId = corrId;
        this.requestIp = ip;
        this.principalName = principal;
        this.issuedAt = System.currentTimeMillis();
    }

    /**
     * Returns the correlation ID.
     *
     * @return correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the request IP address.
     *
     * @return request IP string
     */
    public String getRequestIp() {
        return requestIp;
    }

    /**
     * Returns the principal name.
     *
     * @return principal name string
     */
    public String getPrincipalName() {
        return principalName;
    }

    /**
     * Returns the epoch millisecond timestamp.
     *
     * @return issuedAt timestamp
     */
    public long getIssuedAt() {
        return issuedAt;
    }

    /**
     * Sets the correlation ID.
     *
     * @param value the new correlation ID
     */
    public void setCorrelationId(final String value) {
        this.correlationId = value;
    }

    /**
     * Sets the request IP address.
     *
     * @param value the new request IP
     */
    public void setRequestIp(final String value) {
        this.requestIp = value;
    }

    /**
     * Sets the principal name.
     *
     * @param value the new principal name
     */
    public void setPrincipalName(final String value) {
        this.principalName = value;
    }

    /**
     * Sets the issuedAt timestamp.
     *
     * @param value the new epoch millisecond timestamp
     */
    public void setIssuedAt(final long value) {
        this.issuedAt = value;
    }

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
