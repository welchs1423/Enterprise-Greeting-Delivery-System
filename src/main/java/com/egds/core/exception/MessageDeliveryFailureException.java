package com.egds.core.exception;

/**
 * Signals an unrecoverable failure in the message delivery lifecycle.
 * Thrown when the delivery pipeline encounters a state from which it
 * cannot recover without external intervention.
 */
public class MessageDeliveryFailureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Correlation ID of the message that failed delivery. */
    private final String correlationId;

    /** Machine-readable failure classification code. */
    private final String failureCode;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message human-readable description of the failure
     * @param corrId  correlation ID of the message that failed
     * @param code    machine-readable failure classification code
     */
    public MessageDeliveryFailureException(
            final String message,
            final String corrId,
            final String code) {
        super(message);
        this.correlationId = corrId;
        this.failureCode = code;
    }

    /**
     * Constructs a new exception with a causal exception.
     *
     * @param message human-readable description of the failure
     * @param corrId  correlation ID of the message that failed
     * @param code    machine-readable failure classification code
     * @param cause   the underlying exception that caused this failure
     */
    public MessageDeliveryFailureException(
            final String message,
            final String corrId,
            final String code,
            final Throwable cause) {
        super(message, cause);
        this.correlationId = corrId;
        this.failureCode = code;
    }

    /**
     * Returns the correlation ID of the failed message.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the machine-readable failure classification code.
     *
     * @return the failure code string
     */
    public String getFailureCode() {
        return failureCode;
    }
}
