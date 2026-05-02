package com.enterprise.greeting.exception;

/**
 * Unchecked exception for unrecoverable failures in the message delivery
 * pipeline. Encapsulates the error code and the pipeline layer at which
 * the failure occurred.
 *
 * <p>All fatal errors in the delivery lifecycle must be wrapped in this
 * exception to minimise inter-layer coupling.
 *
 * <p>Typical trigger points:
 * <ul>
 *   <li>Factory instantiation failure</li>
 *   <li>DTO-to-entity mapping failure</li>
 *   <li>Output strategy I/O failure</li>
 * </ul>
 */
public final class MessageDeliveryFailureException extends RuntimeException {

    private static final long serialVersionUID = 8675309L;

    /**
     * Standard identifier following the error-code schema.
     * Format: {@code EGDS-[domain]-[sequence]}
     */
    private final String errorCode;

    /**
     * Identifier for the pipeline layer at which the failure occurred.
     * Examples: {@code FACTORY_LAYER}, {@code MAPPER_LAYER}.
     */
    private final String failedLayer;

    /**
     * Constructs an exception with an error code, failed layer, and message.
     *
     * @param code    standard error code identifier; must not be null
     * @param layer   architecture layer name where the failure occurred
     * @param message human-readable failure description
     */
    public MessageDeliveryFailureException(
            final String code,
            final String layer,
            final String message) {
        super(message);
        this.errorCode = code;
        this.failedLayer = layer;
    }

    /**
     * Constructs an exception with an error code, failed layer, message,
     * and root cause.
     *
     * @param code    standard error code identifier; must not be null
     * @param layer   architecture layer name where the failure occurred
     * @param message human-readable failure description
     * @param cause   the underlying exception that triggered this failure
     */
    public MessageDeliveryFailureException(
            final String code,
            final String layer,
            final String message,
            final Throwable cause) {
        super(message, cause);
        this.errorCode = code;
        this.failedLayer = layer;
    }

    /**
     * Returns the standard error code assigned to this exception.
     *
     * @return the error code string
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the pipeline layer identifier where the failure occurred.
     *
     * @return the failed layer name string
     */
    public String getFailedLayer() {
        return failedLayer;
    }

    /**
     * Returns a human-readable string representation of this exception.
     *
     * @return formatted string including errorCode, failedLayer, and message
     */
    @Override
    public String toString() {
        return "MessageDeliveryFailureException{"
                + "errorCode='" + errorCode + '\''
                + ", failedLayer='" + failedLayer + '\''
                + ", message='" + getMessage() + '\''
                + '}';
    }
}
