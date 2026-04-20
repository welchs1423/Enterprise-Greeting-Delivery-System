package com.egds.core.entity;

import com.egds.core.enums.DeliveryStatus;

/**
 * Domain entity representing a greeting message prepared for output channel delivery.
 * Produced exclusively by {@link com.egds.core.mapper.MessageMapper} from a validated
 * {@link com.egds.core.dto.MessageContentDto}, this entity is the terminal form of
 * a message within the EGDS delivery lifecycle.
 */
public class MessageEntity {

    private final String entityId;
    private final String correlationId;
    private final String formattedContent;
    private final long deliveryTimestamp;
    private DeliveryStatus deliveryStatus;

    /**
     * Constructs a new MessageEntity with the specified attributes.
     * The initial delivery status is set to {@link DeliveryStatus#PENDING}.
     *
     * @param entityId          a unique identifier for this entity instance
     * @param correlationId     the originating DTO's correlation identifier
     * @param formattedContent  the output-ready, formatted message string
     * @param deliveryTimestamp the epoch millisecond timestamp of entity creation
     */
    public MessageEntity(String entityId, String correlationId,
                         String formattedContent, long deliveryTimestamp) {
        this.entityId = entityId;
        this.correlationId = correlationId;
        this.formattedContent = formattedContent;
        this.deliveryTimestamp = deliveryTimestamp;
        this.deliveryStatus = DeliveryStatus.PENDING;
    }

    /**
     * Returns the unique identifier of this entity instance.
     *
     * @return the entity ID string
     */
    public String getEntityId() { return entityId; }

    /**
     * Returns the correlation identifier linking this entity to its originating DTO.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() { return correlationId; }

    /**
     * Returns the output-ready formatted message content.
     *
     * @return the formatted content string
     */
    public String getFormattedContent() { return formattedContent; }

    /**
     * Returns the epoch millisecond timestamp at which this entity was created.
     *
     * @return the delivery timestamp in milliseconds since epoch
     */
    public long getDeliveryTimestamp() { return deliveryTimestamp; }

    /**
     * Returns the current delivery status of this entity.
     *
     * @return the {@link DeliveryStatus} value
     */
    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }

    /**
     * Transitions this entity to the specified delivery status.
     * Status transitions must conform to the EGDS lifecycle state machine.
     *
     * @param deliveryStatus the target {@link DeliveryStatus}
     */
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    @Override
    public String toString() {
        return "MessageEntity{" +
               "entityId='" + entityId + '\'' +
               ", correlationId='" + correlationId + '\'' +
               ", deliveryStatus=" + deliveryStatus +
               ", formattedContent='" + formattedContent + '\'' +
               '}';
    }
}
