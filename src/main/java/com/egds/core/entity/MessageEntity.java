package com.egds.core.entity;

import com.egds.core.enums.DeliveryStatus;

/**
 * Domain entity representing a greeting message prepared for output
 * channel delivery. Produced exclusively by
 * {@link com.egds.core.mapper.MessageMapper} from a validated
 * {@link com.egds.core.dto.MessageContentDto}, this entity is the
 * terminal form of a message within the EGDS delivery lifecycle.
 */
public final class MessageEntity {

    /** Unique identifier for this entity instance. */
    private final String entityId;

    /** Correlation ID linking this entity to its originating DTO. */
    private final String correlationId;

    /** Output-ready, formatted message string. */
    private final String formattedContent;

    /** Epoch millisecond timestamp of entity creation. */
    private final long deliveryTimestamp;

    /** Current delivery status in the EGDS lifecycle state machine. */
    private DeliveryStatus deliveryStatus;

    /**
     * Constructs a new MessageEntity with the specified attributes.
     * The initial delivery status is set to {@link DeliveryStatus#PENDING}.
     *
     * @param id        a unique identifier for this entity instance
     * @param corrId    the originating DTO's correlation identifier
     * @param content   the output-ready, formatted message string
     * @param timestamp the epoch millisecond timestamp of entity creation
     */
    public MessageEntity(
            final String id,
            final String corrId,
            final String content,
            final long timestamp) {
        this.entityId = id;
        this.correlationId = corrId;
        this.formattedContent = content;
        this.deliveryTimestamp = timestamp;
        this.deliveryStatus = DeliveryStatus.PENDING;
    }

    /**
     * Returns the unique identifier of this entity instance.
     *
     * @return the entity ID string
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Returns the correlation ID linking this entity to its originating
     * DTO.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the output-ready formatted message content.
     *
     * @return the formatted content string
     */
    public String getFormattedContent() {
        return formattedContent;
    }

    /**
     * Returns the epoch millisecond timestamp at which this entity was
     * created.
     *
     * @return the delivery timestamp in milliseconds since epoch
     */
    public long getDeliveryTimestamp() {
        return deliveryTimestamp;
    }

    /**
     * Returns the current delivery status of this entity.
     *
     * @return the {@link DeliveryStatus} value
     */
    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    /**
     * Transitions this entity to the specified delivery status.
     *
     * @param status the target {@link DeliveryStatus}
     */
    public void setDeliveryStatus(final DeliveryStatus status) {
        this.deliveryStatus = status;
    }

    @Override
    public String toString() {
        return "MessageEntity{"
                + "entityId='" + entityId + '\''
                + ", correlationId='" + correlationId + '\''
                + ", deliveryStatus=" + deliveryStatus
                + ", formattedContent='" + formattedContent + '\''
                + '}';
    }
}
