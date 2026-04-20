package com.egds.core.provider;

import com.egds.core.dto.MessageContentDto;
import com.egds.core.enums.MessagePriority;
import com.egds.core.interfaces.IMessageProvider;
import java.util.UUID;

/**
 * Canonical implementation of {@link IMessageProvider} for standard greeting delivery.
 * Produces a {@link MessageContentDto} containing the globally recognized greeting payload.
 * This provider represents the reference implementation for EGDS content sourcing.
 */
public class HelloWorldMessageProvider implements IMessageProvider {

    /**
     * Constructs and returns a {@link MessageContentDto} encapsulating the standard
     * "Hello, World!" greeting payload with NORMAL priority and the en-US locale.
     * Each invocation generates a distinct correlation identifier to support distributed tracing.
     *
     * @return a fully populated {@link MessageContentDto} instance
     */
    @Override
    public MessageContentDto provideMessage() {
        return new MessageContentDto.Builder("Hello, World!", UUID.randomUUID().toString())
                .locale("en-US")
                .priority(MessagePriority.NORMAL)
                .build();
    }
}
