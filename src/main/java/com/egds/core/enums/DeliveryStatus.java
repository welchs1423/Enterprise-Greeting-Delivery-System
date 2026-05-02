package com.egds.core.enums;

/**
 * Represents the lifecycle state of a message within the EGDS delivery
 * pipeline. State transitions are managed exclusively by the
 * MessageDeliveryPipeline.
 */
public enum DeliveryStatus {

    /** Message has been accepted and is awaiting pipeline entry. */
    PENDING,

    /** Message is actively traversing the delivery pipeline. */
    IN_TRANSIT,

    /** Message has been successfully delivered to the output strategy. */
    DELIVERED,

    /** Delivery failed; consult MessageDeliveryFailureException. */
    FAILED
}
