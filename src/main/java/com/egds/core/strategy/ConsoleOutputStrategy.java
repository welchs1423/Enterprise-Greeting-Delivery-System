package com.egds.core.strategy;

import com.egds.core.entity.MessageEntity;
import com.egds.core.enums.DeliveryStatus;
import com.egds.core.exception.MessageDeliveryFailureException;
import com.egds.core.interfaces.IMessageOutputStrategy;
import org.springframework.stereotype.Component;

/**
 * {@link IMessageOutputStrategy} implementation targeting the standard
 * output stream. Delivers the formatted content of a
 * {@link MessageEntity} to {@code System.out}.
 * Designated for CLI and non-interactive runtime environments.
 */
@Component
public class ConsoleOutputStrategy implements IMessageOutputStrategy {

    /**
     * Writes the formatted content of the supplied entity to stdout.
     * Transitions the entity's delivery status to IN_TRANSIT prior to
     * write and to DELIVERED on success, or FAILED on error.
     *
     * @param messageEntity the finalized entity to deliver; must not be null
     * @throws MessageDeliveryFailureException if entity is null or output
     *         stream cannot be written to
     */
    @Override
    public void output(final MessageEntity messageEntity) {
        if (messageEntity == null) {
            throw new MessageDeliveryFailureException(
                    "ConsoleOutputStrategy received a null MessageEntity.",
                    "UNKNOWN",
                    "ERR_NULL_ENTITY"
            );
        }
        try {
            messageEntity.setDeliveryStatus(DeliveryStatus.IN_TRANSIT);
            System.out.println(messageEntity.getFormattedContent());
            messageEntity.setDeliveryStatus(DeliveryStatus.DELIVERED);
        } catch (Exception e) {
            messageEntity.setDeliveryStatus(DeliveryStatus.FAILED);
            throw new MessageDeliveryFailureException(
                    "ConsoleOutputStrategy failed during output write.",
                    messageEntity.getCorrelationId(),
                    "ERR_OUTPUT_WRITE_FAILURE",
                    e
            );
        }
    }
}
