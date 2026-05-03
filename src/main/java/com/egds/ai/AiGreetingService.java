package com.egds.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Orchestrates context collection and LLM invocation to produce a
 * dynamically generated greeting string.
 *
 * <p>On application startup, {@link #init()} constructs the
 * {@link AiGreetingAssistant} proxy via
 * {@link AiServices#builder(Class)} backed by an
 * {@link OpenAiChatModel}. Each call to
 * {@link #generateContextualGreeting()} collects fresh runtime metadata
 * from {@link GreetingContextCollector}, serialises it into a prompt
 * context string, and delegates to the assistant proxy.
 *
 * <p>If the OpenAI API key is unavailable or the upstream call fails,
 * the exception propagates to the caller, which is expected to activate
 * the Resilience4j fallback defined in
 * {@link com.egds.core.provider.HelloWorldMessageProvider}.
 */
@Service
public class AiGreetingService {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(AiGreetingService.class);

    /** OpenAI API key sourced from the OPENAI_API_KEY environment variable. */
    @Value("${langchain4j.open-ai.chat-model.api-key:"
            + "dev-placeholder-no-llm-calls}")
    private String openAiApiKey;

    /** OpenAI model name (e.g., gpt-4o). */
    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-4o}")
    private String modelName;

    /** LLM temperature controlling response creativity. */
    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private double temperature;

    /** Collector supplying runtime context metadata for the prompt. */
    private final GreetingContextCollector contextCollector;

    /** LangChain4j assistant proxy, initialised at startup. */
    private AiGreetingAssistant assistant;

    /**
     * @param collector the runtime context metadata collector
     */
    public AiGreetingService(final GreetingContextCollector collector) {
        this.contextCollector = collector;
    }

    /**
     * Constructs the {@link AiGreetingAssistant} proxy after bean
     * properties have been injected. Uses {@link OpenAiChatModel} as the
     * backing {@link ChatLanguageModel}.
     */
    @PostConstruct
    public void init() {
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
        this.assistant = AiServices.builder(AiGreetingAssistant.class)
                .chatLanguageModel(model)
                .build();
        LOG.info("[AI] AiGreetingAssistant initialised model={}",
                modelName);
    }

    /**
     * Collects runtime context metadata and invokes the LLM to generate
     * a contextual greeting string.
     *
     * @return the LLM-generated greeting string
     */
    public String generateContextualGreeting() {
        GreetingContextMetadata metadata = contextCollector.collect();
        String contextPrompt = buildContextPrompt(metadata);
        LOG.debug("[AI] generating greeting context={}", contextPrompt);
        String greeting = assistant.generateGreeting(contextPrompt);
        LOG.info("[AI] greeting generated text='{}'", greeting);
        return greeting;
    }

    /**
     * Serialises the supplied metadata into a human-readable context
     * string suitable for inclusion in the LLM prompt.
     *
     * @param metadata the collected runtime metadata
     * @return the formatted context string
     */
    private String buildContextPrompt(
            final GreetingContextMetadata metadata) {
        return String.format(
                "Client IP: %s | Server CPU Temp: %.1f°C"
                        + " | Timestamp: %s | Locale: %s",
                metadata.getVirtualClientIp(),
                metadata.getCpuTemperatureCelsius(),
                metadata.getCollectedAt(),
                metadata.getServerLocale());
    }
}
