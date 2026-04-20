package com.egds.core.strategy;

import com.egds.core.entity.MessageEntity;
import com.egds.core.enums.DeliveryStatus;
import com.egds.core.exception.MessageDeliveryFailureException;
import com.egds.core.interfaces.IMessageOutputStrategy;

/**
 * {@link IMessageOutputStrategy} implementation targeting the standard output stream.
 * Delivers the formatted content of a {@link MessageEntity} to {@code System.out}.
 * This strategy is designated for CLI and non-interactive runtime environments.
 */
public class ConsoleOutputStrategy implements IMessageOutputStrategy {

    /**
     * Writes the formatted content of the supplied entity to the standard output stream.
     * Transitions the entity's delivery status to IN_TRANSIT prior to write and
     * to DELIVERED on success, or FAILED on error.
     *
     * @param messageEntity the finalized entity to be delivered; must not be null
     * @throws MessageDeliveryFailureException if the entity is null or the output stream
     *         cannot be written to
     */
    @Override
    public void output(MessageEntity messageEntity) {
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
                    "ConsoleOutputStrategy failed during output stream write.",
                    messageEntity.getCorrelationId(),
                    "ERR_OUTPUT_WRITE_FAILURE",
                    e
            );
        }
    }
}
