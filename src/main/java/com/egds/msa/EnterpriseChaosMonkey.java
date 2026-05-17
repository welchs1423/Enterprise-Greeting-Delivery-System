package com.egds.msa;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Per-letter chaos injector for the V22 distributed letter assembler.
 *
 * <p>Each call to {@link #sabotage()} has a
 * {@value #FAILURE_PERCENT}% probability of throwing a
 * {@link RuntimeException}, simulating a downstream microservice
 * timeout or crash. The calling assembler replaces the failed
 * character with a space, producing a gap-riddled greeting.
 *
 * <p>Chaos can be suppressed via
 * {@code egds.msa.chaos.enabled=false} to prevent flaky
 * test output.
 */
@Component
public class EnterpriseChaosMonkey {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(EnterpriseChaosMonkey.class);

    /** Failure probability per sabotage call (5%). */
    private static final int FAILURE_PERCENT = 5;

    /** Denominator for percentage-based probability. */
    private static final int PERCENT = 100;

    /**
     * When false, chaos injection is suppressed for all calls.
     * Controlled by {@code egds.msa.chaos.enabled}.
     */
    @Value("${egds.msa.chaos.enabled:true}")
    private boolean chaosEnabled;

    /**
     * Potentially throws to simulate a microservice failure.
     *
     * <p>When enabled, rolls a random integer in [0, 100). If the
     * roll falls below {@value #FAILURE_PERCENT}, a
     * {@link RuntimeException} is thrown to signal that the
     * virtual letter-microservice has timed out or crashed.
     *
     * @throws RuntimeException {@value #FAILURE_PERCENT}% of the time
     *         when chaos is enabled
     */
    public void sabotage() {
        if (!chaosEnabled) {
            return;
        }
        int roll = ThreadLocalRandom.current().nextInt(PERCENT);
        if (roll < FAILURE_PERCENT) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("[MSA-CHAOS] letter microservice failure roll={}",
                        roll);
            }
            throw new RuntimeException(
                    "Letter microservice timeout (simulated chaos)");
        }
    }

    /**
     * Enables or disables chaos injection.
     *
     * @param enabled {@code true} to activate, {@code false} to suppress
     */
    public void setChaosEnabled(final boolean enabled) {
        this.chaosEnabled = enabled;
    }

    /**
     * Returns whether chaos injection is currently active.
     *
     * @return {@code true} if enabled
     */
    public boolean isChaosEnabled() {
        return chaosEnabled;
    }
}
