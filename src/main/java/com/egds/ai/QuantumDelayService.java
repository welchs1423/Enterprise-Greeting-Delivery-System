package com.egds.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates quantum superposition of greeting observation timing.
 *
 * <p>Models the Schrodinger's-cat thought experiment: the greeting exists in
 * a superposition of "observed" and "unobserved" states until this service
 * collapses the wave function. On each invocation, a fair coin flip
 * determines whether the observation is immediate (|immediate> eigenstate)
 * or deferred by a uniformly-distributed random interval up to
 * {@code egds.quantum.max-delay-ms} milliseconds (|delayed> eigenstate).
 *
 * <p>Intended to be injected into {@link AiGreetingService} and called
 * immediately before the generated greeting is returned to the caller.
 */
@Service
public class QuantumDelayService {

    private static final Logger LOG =
            LoggerFactory.getLogger(QuantumDelayService.class);

    /**
     * Upper bound of the random delay window in milliseconds.
     * Configurable via {@code egds.quantum.max-delay-ms}; defaults to 10 000.
     */
    @Value("${egds.quantum.max-delay-ms:10000}")
    private long maxDelayMs;

    /**
     * Collapses the quantum superposition of the greeting observation.
     *
     * <p>With probability 0.5, the current thread sleeps for a uniformly
     * distributed duration drawn from [0, {@code maxDelayMs}] before
     * returning. With probability 0.5, the method returns immediately.
     *
     * @throws InterruptedException if the thread is interrupted while sleeping;
     *                              the interrupt flag is restored before
     *                              the exception propagates
     */
    public void applyQuantumDelay() throws InterruptedException {
        boolean delayed = ThreadLocalRandom.current().nextBoolean();
        if (delayed) {
            long delayMs = ThreadLocalRandom.current().nextLong(maxDelayMs + 1);
            LOG.info("[QUANTUM] superposition collapsed to delayed eigenstate"
                    + " delay_ms={}", delayMs);
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw ie;
            }
        } else {
            LOG.info("[QUANTUM] superposition collapsed to immediate eigenstate");
        }
    }
}
