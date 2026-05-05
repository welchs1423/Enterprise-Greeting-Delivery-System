package com.egds.ai;

import com.egds.mainframe.As400MainframeEmulator;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

/**
 * Orchestrates the Phase 7 meta-enterprise greeting pipeline.
 *
 * <p>On application startup, {@link #init()} constructs the
 * {@link AiGreetingAssistant} proxy via {@link AiServices#builder(Class)}
 * backed by an {@link OpenAiChatModel}.
 *
 * <p>Each call to {@link #generateContextualGreeting()} executes the
 * following pipeline:
 * <ol>
 *   <li>Collect runtime context metadata via
 *       {@link GreetingContextCollector}.</li>
 *   <li>Provision an ephemeral AWS Lambda via
 *       {@link EphemeralTerraformAdapter#provision(String)}
 *       (Terraform apply mock).</li>
 *   <li>Generate the greeting via the LLM assistant proxy.</li>
 *   <li>Route the greeting through the ephemeral Lambda via
 *       {@link EphemeralTerraformAdapter#invoke(String, String)}.</li>
 *   <li>Persist the greeting to the AS/400 mainframe ledger using a
 *       mock two-phase commit via {@link As400MainframeEmulator}.</li>
 *   <li>Apply a TensorFlow-inferred delivery delay via
 *       {@link TensorFlowDelayPredictor}.</li>
 *   <li>Destroy the ephemeral Lambda via
 *       {@link EphemeralTerraformAdapter#destroy(String)}
 *       (Terraform destroy mock).</li>
 * </ol>
 *
 * <p>If the OpenAI API key is unavailable or the upstream call fails,
 * the exception propagates to the caller, which is expected to activate
 * the Resilience4j fallback defined in
 * {@link com.egds.core.provider.HelloWorldMessageProvider}.
 */
@Service
public class AiGreetingService {

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

    /**
     * TensorFlow JNI delay predictor applied after Lambda invocation.
     * Replaces the Phase 6 {@link QuantumDelayService}.
     */
    private final TensorFlowDelayPredictor delayPredictor;

    /** Terraform adapter for ephemeral Lambda provisioning lifecycle. */
    private final EphemeralTerraformAdapter terraformAdapter;

    /** AS/400 mainframe emulator for dual-ledger EBCDIC persistence. */
    private final As400MainframeEmulator mainframeEmulator;

    /** LangChain4j assistant proxy, initialised at startup. */
    private AiGreetingAssistant assistant;

    /**
     * @param collector          the runtime context metadata collector
     * @param delayPredictor     the TensorFlow-based delivery delay predictor
     * @param terraformAdapter   the ephemeral Terraform Lambda lifecycle adapter
     * @param mainframeEmulator  the AS/400 EBCDIC dual-ledger emulator
     */
    public AiGreetingService(
            final GreetingContextCollector collector,
            final TensorFlowDelayPredictor delayPredictor,
            final EphemeralTerraformAdapter terraformAdapter,
            final As400MainframeEmulator mainframeEmulator) {
        this.contextCollector = collector;
        this.delayPredictor = delayPredictor;
        this.terraformAdapter = terraformAdapter;
        this.mainframeEmulator = mainframeEmulator;
    }

    /**
     * Constructs the {@link AiGreetingAssistant} proxy after bean
     * properties have been injected.
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
        LOG.info("[AI] AiGreetingAssistant initialised model={}", modelName);
    }

    /**
     * Executes the Phase 7 ephemeral-infrastructure greeting pipeline.
     *
     * <p>Provisions a single-use AWS Lambda (mock), generates an
     * AI-contextualised greeting, routes it through the Lambda, persists
     * it to the AS/400 mainframe ledger via 2PC, applies a TF-predicted
     * delivery delay, then destroys all ephemeral infrastructure before
     * returning the greeting to the caller.
     *
     * <p>The ephemeral Lambda is always destroyed in a {@code finally}
     * block to prevent infrastructure leaks if the 2PC or delay step
     * throws.
     *
     * @return the LLM-generated greeting string, after full pipeline
     *         execution
     */
    public String generateContextualGreeting() {
        String correlationId = UUID.randomUUID().toString();
        GreetingContextMetadata metadata = contextCollector.collect();
        String contextPrompt = buildContextPrompt(metadata);
        LOG.debug("[AI] generating greeting context={}", contextPrompt);

        String lambdaArn = terraformAdapter.provision(correlationId);
        try {
            String greeting = assistant.generateGreeting(contextPrompt);
            LOG.info("[AI] LLM greeting generated correlationId={}",
                    correlationId);

            greeting = terraformAdapter.invoke(lambdaArn, greeting);

            mainframeEmulator.prepare(correlationId, greeting);
            mainframeEmulator.commit(correlationId);

            try {
                delayPredictor.applyPredictedDelay();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                LOG.warn("[AI] TF delay interrupted, returning immediately"
                        + " correlationId={}", correlationId);
            }

            LOG.info("[AI] pipeline complete correlationId={}", correlationId);
            return greeting;
        } finally {
            terraformAdapter.destroy(correlationId);
        }
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
                "Client IP: %s | Server CPU Temp: %.1f C"
                        + " | Timestamp: %s | Locale: %s",
                metadata.getVirtualClientIp(),
                metadata.getCpuTemperatureCelsius(),
                metadata.getCollectedAt(),
                metadata.getServerLocale());
    }
}
