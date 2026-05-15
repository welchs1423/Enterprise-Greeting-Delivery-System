package com.egds.core.provider;

import com.egds.ai.AiGreetingService;
import com.egds.blockchain.GreetingIntegrityVerifier;
import com.egds.core.dto.MessageContentDto;
import com.egds.core.enums.MessagePriority;
import com.egds.core.interfaces.IMessageProvider;
import com.egds.genai.GenAiHallucinationDecorator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * {@link IMessageProvider} implementation backed by the LangChain4j
 * generative AI context routing layer.
 *
 * <p>Replaces the Phase 4 cache-sourced static greeting with a
 * dynamically generated response from {@link AiGreetingService}.  The
 * LLM prompt is enriched with virtual client IP, simulated CPU
 * temperature, and the current timestamp collected at invocation time.
 *
 * <p>After the AI generates the greeting content, the pre-formatted
 * string is registered with {@link GreetingIntegrityVerifier} to record
 * a Keccak-256 fingerprint in the mock Ethereum smart contract. The
 * same fingerprint is later verified by
 * {@link com.egds.core.strategy.ConsoleOutputStrategy} before any output
 * is written to the channel.
 *
 * <p>If the LLM call fails (network error, quota exceeded, or invalid
 * API key), the exception propagates through the pipeline and triggers
 * the Resilience4j fallback in
 * {@link com.egds.core.strategy.ConsoleOutputStrategy}.
 */
@Component
public class HelloWorldMessageProvider implements IMessageProvider {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(HelloWorldMessageProvider.class);

    /** Fixed locale tag applied to all messages from this provider. */
    private static final String LOCALE = "en-US";

    /** Fixed priority applied to all messages from this provider. */
    private static final MessagePriority PRIORITY = MessagePriority.NORMAL;

    /** AI service supplying the dynamically generated greeting. */
    private final AiGreetingService aiGreetingService;

    /** Blockchain verifier for pre-delivery content fingerprinting. */
    private final GreetingIntegrityVerifier integrityVerifier;

    /** V18 hallucination decorator applied before hash registration. */
    private final GenAiHallucinationDecorator hallucinationDecorator;

    /**
     * @param aiService    the generative AI greeting service
     * @param verifier     the blockchain integrity verifier
     * @param decorator    the V18 hallucination decorator
     */
    public HelloWorldMessageProvider(
            final AiGreetingService aiService,
            final GreetingIntegrityVerifier verifier,
            final GenAiHallucinationDecorator decorator) {
        this.aiGreetingService = aiService;
        this.integrityVerifier = verifier;
        this.hallucinationDecorator = decorator;
    }

    /**
     * Generates an AI-contextualised greeting and registers its
     * blockchain fingerprint before returning the delivery DTO.
     *
     * <p>Processing steps:
     * <ol>
     *   <li>Assign a new UUID correlation identifier.</li>
     *   <li>Invoke {@link AiGreetingService#generateContextualGreeting()}
     *       to obtain the LLM response.</li>
     *   <li>Pre-compute the formatted string that
     *       {@link com.egds.core.mapper.MessageMapper} will produce
     *       ({@code [PRIORITY][LOCALE] content}) and register its
     *       Keccak-256 hash with the mock smart contract.</li>
     *   <li>Return the {@link MessageContentDto} carrying the raw AI
     *       content.</li>
     * </ol>
     *
     * @return a fully populated {@link MessageContentDto} instance
     */
    @Override
    public MessageContentDto provideMessage() {
        String correlationId = UUID.randomUUID().toString();

        String content = aiGreetingService.generateContextualGreeting();
        content = hallucinationDecorator.decorate(content);

        // Pre-compute the formatted string MessageMapper will produce
        // to register the exact bytes that ConsoleOutputStrategy will verify.
        String preFormattedContent = String.format("[%s][%s] %s",
                PRIORITY.name(), LOCALE, content);
        integrityVerifier.register(correlationId, preFormattedContent);

        LOG.info("[PROVIDER] AI greeting generated correlationId={}",
                correlationId);

        return new MessageContentDto.Builder(content, correlationId)
                .locale(LOCALE)
                .priority(PRIORITY)
                .build();
    }
}
