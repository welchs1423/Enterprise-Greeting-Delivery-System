package com.egds.core.dto;

import com.egds.core.entity.MessageEntity;

/**
 * Value object encapsulating the outcome of a single message delivery
 * lifecycle execution. Returned by
 * {@link com.egds.core.interfaces.IMessageDeliveryService#deliver()} to
 * provide the caller with auditable delivery metadata.
 * Instances are constructed via the static factory methods
 * {@link #success} and {@link #failure}.
 */
public final class MessageDeliveryResult {

    /** Whether the delivery lifecycle completed without error. */
    private final boolean success;

    /** Correlation ID associated with this delivery result. */
    private final String correlationId;

    /** Total pipeline execution duration in milliseconds. */
    private final long deliveryDurationMs;

    /** Entity that was delivered, or null if delivery failed. */
    private final MessageEntity deliveredEntity;

    /** Failure reason description, or null if delivery succeeded. */
    private final String failureReason;

    private MessageDeliveryResult(
            final boolean isSuccess,
            final String corrId,
            final long durationMs,
            final MessageEntity entity,
            final String reason) {
        this.success = isSuccess;
        this.correlationId = corrId;
        this.deliveryDurationMs = durationMs;
        this.deliveredEntity = entity;
        this.failureReason = reason;
    }

    /**
     * Constructs a successful delivery result.
     *
     * @param correlationId      the correlation ID of the delivered message
     * @param deliveryDurationMs the total pipeline execution time in ms
     * @param deliveredEntity    the entity that was successfully delivered
     * @return a success-state {@link MessageDeliveryResult}
     */
    public static MessageDeliveryResult success(
            final String correlationId,
            final long deliveryDurationMs,
            final MessageEntity deliveredEntity) {
        return new MessageDeliveryResult(
                true, correlationId, deliveryDurationMs,
                deliveredEntity, null);
    }

    /**
     * Constructs a failed delivery result.
     *
     * @param correlationId      the correlation ID of the failed message
     * @param deliveryDurationMs the total pipeline execution time in ms
     * @param failureReason      human-readable explanation of the failure
     * @return a failure-state {@link MessageDeliveryResult}
     */
    public static MessageDeliveryResult failure(
            final String correlationId,
            final long deliveryDurationMs,
            final String failureReason) {
        return new MessageDeliveryResult(
                false, correlationId, deliveryDurationMs,
                null, failureReason);
    }

    /**
     * Returns whether the delivery lifecycle completed without error.
     *
     * @return true if delivery succeeded; false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the correlation ID associated with this delivery result.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the total pipeline execution duration in milliseconds.
     *
     * @return the delivery duration in milliseconds
     */
    public long getDeliveryDurationMs() {
        return deliveryDurationMs;
    }

    /**
     * Returns the entity that was delivered, or null if delivery failed.
     *
     * @return the {@link MessageEntity} or null
     */
    public MessageEntity getDeliveredEntity() {
        return deliveredEntity;
    }

    /**
     * Returns the failure reason description, or null if successful.
     *
     * @return the failure reason string or null
     */
    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String toString() {
        return "MessageDeliveryResult{"
                + "success=" + success
                + ", correlationId='" + correlationId + '\''
                + ", deliveryDurationMs=" + deliveryDurationMs
                + (failureReason != null
                        ? ", failureReason='" + failureReason + '\''
                        : "")
                + '}';
    }
}
