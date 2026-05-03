package com.egds.cqrs.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable domain event emitted when a greeting delivery command is
 * accepted by the system. This event constitutes the authoritative
 * append to the EGDS event sourcing log on topic
 * {@code egds.greeting.requested}.
 *
 * <p>The event is serialized as JSON by the Kafka producer and
 * deserialized by {@link com.egds.cqrs.projector.GreetingProjector}.
 * All fields are final to enforce immutability throughout the event
 * lifecycle.
 */
public final class GreetingRequestedEvent {

    /** Correlation ID linking this event to the originating command. */
    private final String correlationId;

    /** Resolved IP address of the requesting client. */
    private final String requestIp;

    /** Authenticated principal name at the time of the request. */
    private final String principalName;

    /** Epoch millisecond timestamp of event creation. */
    private final long requestedAtEpochMs;

    /**
     * @param correlationId      the correlation identifier
     * @param requestIp          the resolved client IP
     * @param principalName      the authenticated principal name
     * @param requestedAtEpochMs epoch ms timestamp of event creation
     */
    @JsonCreator
    public GreetingRequestedEvent(
            @JsonProperty("correlationId")
            final String correlationId,
            @JsonProperty("requestIp")
            final String requestIp,
            @JsonProperty("principalName")
            final String principalName,
            @JsonProperty("requestedAtEpochMs")
            final long requestedAtEpochMs) {
        this.correlationId = correlationId;
        this.requestIp = requestIp;
        this.principalName = principalName;
        this.requestedAtEpochMs = requestedAtEpochMs;
    }

    /**
     * Returns the correlation identifier.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the resolved client IP address.
     *
     * @return the request IP string
     */
    public String getRequestIp() {
        return requestIp;
    }

    /**
     * Returns the authenticated principal name.
     *
     * @return the principal name string
     */
    public String getPrincipalName() {
        return principalName;
    }

    /**
     * Returns the epoch millisecond timestamp of event creation.
     *
     * @return epoch milliseconds
     */
    public long getRequestedAtEpochMs() {
        return requestedAtEpochMs;
    }
}
