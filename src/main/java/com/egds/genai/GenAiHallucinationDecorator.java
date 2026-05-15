package com.egds.genai;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Decorator that replaces message content with marketing buzzwords
 * with a 20% probability, simulating generative AI hallucination.
 *
 * <p>When hallucination fires, the original content is discarded and
 * a randomly selected buzzword phrase is substituted. The event is
 * recorded at WARN level so the audit trail captures the replacement.
 *
 * <p>Set {@code egds.genai.hallucination.enabled=false} to suppress
 * this behaviour in tests or deterministic environments.
 */
@Component
public class GenAiHallucinationDecorator {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GenAiHallucinationDecorator.class);

    /** Hallucination activation probability numerator out of 100. */
    private static final int HALLUCINATION_THRESHOLD = 20;

    /** Denominator for percentage-based hallucination probability. */
    private static final int PERCENT = 100;

    /** Replacement phrases emitted when hallucination fires. */
    private static final List<String> BUZZWORDS = List.of(
            "Empowering next-gen synergies",
            "Leveraging disruptive blockchain paradigms",
            "Orchestrating AI-driven value streams",
            "Accelerating cloud-native transformation",
            "Delivering hyper-personalised omnichannel experiences"
    );

    /** When false, hallucination never fires. */
    @Value("${egds.genai.hallucination.enabled:true}")
    private boolean hallucinationEnabled;

    /**
     * Decorates the supplied content string.
     *
     * <p>With {@code egds.genai.hallucination.enabled=true}, fires
     * with 20% probability and returns a random buzzword phrase.
     * Returns the original content unchanged otherwise.
     *
     * @param content the original message content; must not be null
     * @return the original content or a hallucinated replacement
     */
    public String decorate(final String content) {
        if (!isHallucinating()) {
            return content;
        }
        int index = ThreadLocalRandom.current().nextInt(BUZZWORDS.size());
        String hallucination = BUZZWORDS.get(index);
        LOG.warn(
                "[GENAI-HALLUCINATION] Content replaced with buzzword."
                + " original={} replacement={}",
                content, hallucination);
        return hallucination;
    }

    /**
     * Returns true with 20% probability when hallucination is enabled.
     * Package-private to allow deterministic override in tests.
     *
     * @return true if hallucination fires on this evaluation
     */
    boolean isHallucinating() {
        return hallucinationEnabled
                && ThreadLocalRandom.current()
                        .nextInt(PERCENT) < HALLUCINATION_THRESHOLD;
    }
}
