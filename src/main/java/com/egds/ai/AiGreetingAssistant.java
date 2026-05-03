package com.egds.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * LangChain4j {@code AiServices} interface for contextual greeting
 * generation.
 *
 * <p>At runtime this interface is backed by a proxy created via
 * {@link dev.langchain4j.service.AiServices#builder(Class)},
 * injecting a {@link dev.langchain4j.model.chat.ChatLanguageModel}.
 * The system message anchors the LLM to enterprise greeting constraints;
 * the user message supplies the collected context signals.
 *
 * <p>The proxy instance is registered as a Spring bean by
 * {@link AiGreetingService} and is safe for concurrent use.
 */
public interface AiGreetingAssistant {

    /**
     * Generates a contextual greeting using the supplied runtime metadata.
     *
     * <p>The LLM is instructed to produce a single short greeting string
     * of the form "Hello, [context-aware variation]!" that reflects the
     * supplied operational signals. The response must not exceed one
     * sentence and must remain professional for a B2B audience.
     *
     * @param context a structured string summary of the runtime context
     *                produced by {@link GreetingContextCollector}
     * @return the LLM-generated greeting string
     */
    @SystemMessage(
        "You are the EGDS (Enterprise Greeting Delivery System) language "
        + "model. Your sole responsibility is to produce a single, "
        + "professional, B2B-appropriate greeting string. "
        + "The greeting must contain the phrase 'Hello' and must not "
        + "exceed one sentence. Do not explain your reasoning. "
        + "Return only the greeting string."
    )
    @UserMessage(
        "Generate a contextual greeting for the following runtime "
        + "environment snapshot:\n{{context}}\n"
        + "The greeting should subtly reflect the operational context "
        + "while remaining a valid 'Hello, World!' variant."
    )
    String generateGreeting(@V("context") String context);
}
