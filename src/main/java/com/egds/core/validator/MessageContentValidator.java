package com.egds.core.validator;

import com.egds.core.dto.MessageContentDto;
import com.egds.core.exception.MessageDeliveryFailureException;
import com.egds.core.interfaces.IMessageValidator;
import org.springframework.stereotype.Component;

/**
 * Reference implementation of {@link IMessageValidator} enforcing the EGDS
 * message content integrity specification.
 * Validates that mandatory fields are populated and that content does not exceed
 * the maximum permitted payload size defined by the EGDS payload contract.
 */
@Component
public class MessageContentValidator implements IMessageValidator {

    /**
     * Maximum permissible length for a message content string, in characters.
     */
    private static final int MAX_CONTENT_LENGTH = 4096;

    /**
     * Validates the structural and semantic integrity of the supplied {@link MessageContentDto}.
     * Enforces null checks, mandatory field presence, and content length constraints.
     *
     * @param messageContentDto the message payload to validate; must not be null
     * @throws MessageDeliveryFailureException if any integrity constraint is violated
     */
    @Override
    public void validate(MessageContentDto messageContentDto) {
        if (messageContentDto == null) {
            throw new MessageDeliveryFailureException(
                    "Validation failed: MessageContentDto is null.",
                    "UNKNOWN",
                    "ERR_VALIDATION_NULL_DTO"
            );
        }
        if (messageContentDto.getContent() == null || messageContentDto.getContent().isEmpty()) {
            throw new MessageDeliveryFailureException(
                    "Validation failed: message content is null or empty.",
                    messageContentDto.getCorrelationId(),
                    "ERR_VALIDATION_EMPTY_CONTENT"
            );
        }
        if (messageContentDto.getContent().length() > MAX_CONTENT_LENGTH) {
            throw new MessageDeliveryFailureException(
                    "Validation failed: message content exceeds maximum permitted length of "
                            + MAX_CONTENT_LENGTH + " characters.",
                    messageContentDto.getCorrelationId(),
                    "ERR_VALIDATION_CONTENT_TOO_LARGE"
            );
        }
        if (messageContentDto.getCorrelationId() == null || messageContentDto.getCorrelationId().isEmpty()) {
            throw new MessageDeliveryFailureException(
                    "Validation failed: correlationId is null or empty.",
                    "UNKNOWN",
                    "ERR_VALIDATION_MISSING_CORRELATION_ID"
            );
        }
    }
}
