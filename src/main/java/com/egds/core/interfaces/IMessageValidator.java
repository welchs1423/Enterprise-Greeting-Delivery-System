package com.egds.core.interfaces;

import com.egds.core.dto.MessageContentDto;

/**
 * Contract for components that enforce structural and semantic integrity
 * constraints on incoming {@link MessageContentDto} payloads prior to
 * pipeline progression.
 */
public interface IMessageValidator {

    /**
     * Validates the structural and semantic integrity of the supplied DTO.
     *
     * @param messageContentDto the payload to validate; must not be null
     * @throws com.egds.core.exception.MessageDeliveryFailureException
     *         if the payload violates one or more integrity constraints
     */
    void validate(MessageContentDto messageContentDto);
}
