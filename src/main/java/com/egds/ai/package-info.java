/**
 * Generative AI context routing layer powered by LangChain4j.
 *
 * <p>Replaces the hardcoded "Hello, World!" greeting with a dynamically
 * generated response produced by an LLM. The LLM prompt is enriched with
 * contextual metadata (virtual client IP, simulated server CPU
 * temperature, current timestamp) gathered by
 * {@link com.egds.ai.GreetingContextCollector}.
 *
 * <p>Key components:
 * <ul>
 *   <li>{@link com.egds.ai.GreetingContextMetadata} — immutable value
 *       object carrying the collected context signals.</li>
 *   <li>{@link com.egds.ai.GreetingContextCollector} — assembles
 *       metadata from the runtime environment.</li>
 *   <li>{@link com.egds.ai.AiGreetingAssistant} — LangChain4j
 *       {@code AiServices} interface; annotated with system and user
 *       message templates.</li>
 *   <li>{@link com.egds.ai.AiGreetingService} — orchestrates context
 *       collection and LLM invocation; injected as the
 *       {@link com.egds.core.interfaces.IMessageProvider} implementation
 *       backing {@link com.egds.core.provider.HelloWorldMessageProvider}.
 *       </li>
 * </ul>
 */
package com.egds.ai;
