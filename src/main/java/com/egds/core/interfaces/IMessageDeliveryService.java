package com.egds.core.interfaces;

import com.egds.core.dto.MessageDeliveryResult;

/**
 * Top-level service contract governing the end-to-end greeting delivery lifecycle.
 * Implementations are responsible for orchestrating provider retrieval, validation,
 * mapping, aspect application, and output strategy invocation.
 */
public interface IMessageDeliveryService {

    /**
     * Initiates and executes the complete message delivery lifecycle.
     *
     * @return a {@link MessageDeliveryResult} containing delivery outcome metadata
     * @throws com.egds.core.exception.MessageDeliveryFailureException if any stage
     *         of the delivery pipeline encounters an unrecoverable error
     */
    MessageDeliveryResult deliver();
}
