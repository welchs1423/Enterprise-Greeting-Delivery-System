package com.egds.core.exception;

/**
 * Signals an unrecoverable failure in the message delivery lifecycle.
 * This exception is thrown when the delivery pipeline encounters a state
 * from which it cannot recover without external intervention.
 */
public class MessageDeliveryFailureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String correlationId;
    private final String failureCode;

    /**
     * Constructs a new MessageDeliveryFailureException with the specified detail message.
     *
     * @param message       a human-readable description of the failure condition
     * @param correlationId the correlation identifier of the message that failed delivery
     * @param failureCode   a machine-readable failure classification code
     */
    public MessageDeliveryFailureException(String message, String correlationId, String failureCode) {
        super(message);
        this.correlationId = correlationId;
        this.failureCode = failureCode;
    }

    /**
     * Constructs a new MessageDeliveryFailureException with a causal exception.
     *
     * @param message       a human-readable description of the failure condition
     * @param correlationId the correlation identifier of the message that failed delivery
     * @param failureCode   a machine-readable failure classification code
     * @param cause         the underlying exception that precipitated this failure
     */
    public MessageDeliveryFailureException(String message, String correlationId, String failureCode, Throwable cause) {
        super(message, cause);
        this.correlationId = correlationId;
        this.failureCode = failureCode;
    }

    /**
     * Returns the correlation identifier associated with the failed message.
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
