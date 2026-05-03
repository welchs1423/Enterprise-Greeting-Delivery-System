package com.egds.cqrs.command;

/**
 * Command expressing the intent to initiate a greeting delivery cycle.
 *
 * <p>Commands are immutable value objects carrying all data required by
 * the handler to validate intent and publish the corresponding domain
 * event. This command is dispatched by
 * {@link com.egds.web.GreetingController} and handled exclusively by
 * {@link GreetingCommandHandler}.
 */
public final class DeliverGreetingCommand {

    /** Client-assigned or server-generated correlation identifier. */
    private final String correlationId;

    /** Resolved IP address of the requesting client. */
    private final String requestIp;

    /** Authenticated principal name from the security context. */
    private final String principalName;

    /**
     * @param correlationId the correlation identifier for this delivery
     * @param requestIp     the resolved client IP address
     * @param principalName the authenticated principal name
     */
    public DeliverGreetingCommand(
            final String correlationId,
            final String requestIp,
            final String principalName) {
        this.correlationId = correlationId;
        this.requestIp = requestIp;
        this.principalName = principalName;
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
}
