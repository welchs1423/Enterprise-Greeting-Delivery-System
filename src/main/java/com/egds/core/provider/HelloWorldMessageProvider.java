package com.egds.core.provider;

import com.egds.core.dto.MessageContentDto;
import com.egds.core.enums.MessagePriority;
import com.egds.core.interfaces.IMessageProvider;
import com.egds.core.service.GreetingCacheService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Canonical implementation of {@link IMessageProvider} for standard greeting delivery.
 * Retrieves the assembled greeting content from the {@link GreetingCacheService} cache
 * layer to eliminate redundant string assembly on repeated invocations.
 *
 * <p>On the first call within a JVM lifetime (cache miss), the cache service assembles
 * and stores the greeting string. Subsequent calls return the cached value without
 * re-executing the assembly logic. Each call generates a distinct correlation identifier
 * to support distributed tracing across delivery cycles.
 */
@Component
public class HelloWorldMessageProvider implements IMessageProvider {

    private final GreetingCacheService greetingCacheService;

    public HelloWorldMessageProvider(GreetingCacheService greetingCacheService) {
        this.greetingCacheService = greetingCacheService;
    }

    /**
     * Constructs and returns a {@link MessageContentDto} encapsulating the standard
     * greeting payload. Content is sourced from the distributed cache via
     * {@link GreetingCacheService#assembleGreeting(String, String)}.
     *
     * @return a fully populated {@link MessageContentDto} instance
     */
    @Override
    public MessageContentDto provideMessage() {
        String content = greetingCacheService.assembleGreeting("Hello", "World");
        return new MessageContentDto.Builder(content, UUID.randomUUID().toString())
                .locale("en-US")
                .priority(MessagePriority.NORMAL)
                .build();
    }
}
