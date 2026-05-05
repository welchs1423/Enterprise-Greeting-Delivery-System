package com.egds.chaos;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Chaos engineering component that randomly disrupts pipeline execution.
 *
 * <p>On each {@link #unleash()} call there is a
 * {@value #CHAOS_PROBABILITY_PERCENT}% chance of a chaotic event. When
 * triggered, the event is either a 5-second latency injection or an
 * {@link InterruptedException} thrown on the calling thread, selected by
 * a secondary roll. The disruption ratio between the two modes is
 * controlled by {@value #INTERRUPT_THRESHOLD}.
 *
 * <p>Chaos injection can be suppressed via {@link #setEnabled(boolean)}
 * to allow deterministic unit tests without timing side-effects.
 */
@Component
public class EmbeddedChaosMonkey {

    private static final Logger LOG =
        LoggerFactory.getLogger(EmbeddedChaosMonkey.class);

    /** Probability of a chaotic event per {@link #unleash()} call. */
    private static final int CHAOS_PROBABILITY_PERCENT = 10;

    /** Upper bound for the random roll used as a percentage denominator. */
    private static final int PERCENT_DENOMINATOR = 100;

    /** Latency injected when chaos selects the slow-path mode. */
    private static final long LATENCY_INJECTION_MS = 5000L;

    /**
     * Roll values below this threshold select interrupt mode;
     * values from this threshold up to
     * {@link #CHAOS_PROBABILITY_PERCENT} select latency mode.
     */
    private static final int INTERRUPT_THRESHOLD = 5;

    private volatile boolean enabled;

    /**
     * Constructs the monkey with its initial enabled state.
     *
     * @param chaosEnabled {@code true} to activate disruption on startup;
     *                     controlled by {@code egds.chaos.enabled} property
     */
    public EmbeddedChaosMonkey(
            @Value("${egds.chaos.enabled:true}") final boolean chaosEnabled) {
        this.enabled = chaosEnabled;
    }

    /**
     * Potentially disrupts the calling thread.
     *
     * <p>Has a {@value #CHAOS_PROBABILITY_PERCENT}% chance of either
     * blocking for {@value #LATENCY_INJECTION_MS} ms or throwing
     * {@link InterruptedException}. When disabled, returns immediately.
     *
     * @throws InterruptedException when chaos selects interrupt mode
     */
    @SuppressWarnings("PMD.DoNotCallSystemExit")
    public void unleash() throws InterruptedException {
        if (!enabled) {
            return;
        }
        int roll = ThreadLocalRandom.current()
            .nextInt(PERCENT_DENOMINATOR);
        if (roll >= CHAOS_PROBABILITY_PERCENT) {
            return;
        }
        if (roll < INTERRUPT_THRESHOLD) {
            LOG.warn("ChaosMonkey: injecting InterruptedException");
            throw new InterruptedException(
                "EmbeddedChaosMonkey thread disruption");
        }
        LOG.warn("ChaosMonkey: injecting {}ms latency",
            LATENCY_INJECTION_MS);
        Thread.sleep(LATENCY_INJECTION_MS);
    }

    /**
     * Enables or disables chaos injection.
     *
     * @param chaosEnabled {@code true} to enable, {@code false} to suppress
     */
    public void setEnabled(final boolean chaosEnabled) {
        this.enabled = chaosEnabled;
    }

    /**
     * Returns whether chaos injection is currently active.
     *
     * @return {@code true} if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
}
