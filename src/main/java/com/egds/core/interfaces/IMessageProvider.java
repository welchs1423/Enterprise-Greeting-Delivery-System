package com.egds.core.interfaces;

import com.egds.core.dto.MessageContentDto;

/**
 * Contract for components responsible for supplying greeting message payloads.
 * Implementations encapsulate the source and construction logic of raw message content,
 * decoupling the delivery pipeline from any specific message origin.
 */
public interface IMessageProvider {

    /**
     * Produces a fully populated {@link MessageContentDto} representing a single greeting payload.
     *
     * @return a non-null {@link MessageContentDto} instance
     * @throws com.egds.core.exception.MessageDeliveryFailureException if the message content
     *         cannot be constructed or sourced
     */
    MessageContentDto provideMessage();
}
