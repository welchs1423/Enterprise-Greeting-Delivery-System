package com.egds.core.interfaces;

import com.egds.core.entity.MessageEntity;

/**
 * Strategy contract defining the output channel for message delivery.
 * Implementations determine how and where a finalized {@link MessageEntity} is rendered,
 * enabling runtime substitution of delivery channels without pipeline modification.
 */
public interface IMessageOutputStrategy {

    /**
     * Executes the delivery of the supplied message entity to the designated output channel.
     *
     * @param messageEntity the finalized entity to be delivered; must not be null
     * @throws com.egds.core.exception.MessageDeliveryFailureException if the output channel
     *         is unavailable or the entity cannot be rendered
     */
    void output(MessageEntity messageEntity);
}
