package com.egds.msa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * V22 enterprise-grade distributed letter assembler.
 *
 * <p>Accepts a greeting string and reassembles it character by
 * character. Each character is fetched by a simulated virtual
 * microservice call via {@link CompletableFuture#supplyAsync},
 * subject to a random 50–200 ms network round-trip delay.
 *
 * <p>An {@link EnterpriseChaosMonkey} is injected into each
 * letter-fetch operation. With 5% probability per character the
 * chaos monkey throws, and the failed character is replaced with
 * a space ({@code ' '}), producing an endearingly gap-riddled
 * greeting.
 *
 * <p>Disable via {@code egds.msa.assembler.enabled=false} to
 * return the original string without delay or chaos (required in
 * test environments to avoid slow parallel futures).
 */
@Component
public class DistributedLetterAssembler {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(DistributedLetterAssembler.class);

    /** Minimum simulated microservice latency in milliseconds. */
    private static final long DELAY_MIN_MS = 50L;

    /** Maximum simulated microservice latency in milliseconds. */
    private static final long DELAY_MAX_MS = 201L;

    /** Space character substituted when a letter microservice fails. */
    private static final char FALLBACK_CHAR = ' ';

    /**
     * When false, {@link #assemble(String)} returns the input
     * unchanged without spawning any futures.
     */
    @Value("${egds.msa.assembler.enabled:true}")
    private boolean assemblerEnabled;

    /** Per-letter chaos injector. */
    private final EnterpriseChaosMonkey chaosMonkey;

    /**
     * @param monkey the per-letter chaos injector
     */
    public DistributedLetterAssembler(
            final EnterpriseChaosMonkey monkey) {
        this.chaosMonkey = monkey;
    }

    /**
     * Reassembles {@code text} character by character using parallel
     * virtual microservice calls, each subject to random network
     * latency and chaos-monkey disruption.
     *
     * <p>When the assembler is disabled or {@code text} is null,
     * the input is returned unchanged.
     *
     * @param text the greeting string to reassemble
     * @return the assembled string, possibly with chaos-induced gaps
     */
    public String assemble(final String text) {
        if (!assemblerEnabled || text == null) {
            return text;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("[MSA-ASSEMBLER] dispatching {} letter futures",
                    text.length());
        }
        char[] chars = text.toCharArray();
        List<CompletableFuture<Character>> futures =
                new ArrayList<>(chars.length);
        for (char c : chars) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> fetchLetter(c)));
        }
        StringBuilder result = new StringBuilder(chars.length);
        for (CompletableFuture<Character> future : futures) {
            try {
                result.append(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                result.append(FALLBACK_CHAR);
            } catch (ExecutionException e) {
                result.append(FALLBACK_CHAR);
            }
        }
        return result.toString();
    }

    /**
     * Simulates a virtual letter-microservice call for a single
     * character. Sleeps for a random duration in
     * [{@value #DELAY_MIN_MS}, {@value #DELAY_MAX_MS}) ms, then
     * optionally invokes the chaos monkey before returning the
     * character.
     *
     * @param c the character to fetch
     * @return the original character, or {@value #FALLBACK_CHAR}
     *         if chaos disruption is triggered
     */
    Character fetchLetter(final char c) {
        long delay = ThreadLocalRandom.current()
                .nextLong(DELAY_MIN_MS, DELAY_MAX_MS);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            chaosMonkey.sabotage();
            return c;
        } catch (RuntimeException e) {
            return FALLBACK_CHAR;
        }
    }
}
