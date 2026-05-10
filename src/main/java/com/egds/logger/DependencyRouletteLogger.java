package com.egds.logger;

import java.util.Random;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Custom logger that randomly selects one of four backends for each
 * log call: {@code java.util.logging}, {@code System.out},
 * {@code System.err}, and SLF4J.
 *
 * <p>Backend selection is uniform and independent per invocation.
 * The intent is to route log output through an unpredictable sink
 * sequence, maximising diagnostic entropy across the delivery
 * pipeline.</p>
 */
@Component
public class DependencyRouletteLogger {

    /** SLF4J logger used as one of the four backends. */
    private static final Logger SLF4J_LOG =
            LoggerFactory.getLogger(DependencyRouletteLogger.class);

    /** JUL logger used as one of the four backends. */
    private static final java.util.logging.Logger JUL_LOG =
            java.util.logging.Logger.getLogger(
                    DependencyRouletteLogger.class.getName());

    /** Number of available logging backends. */
    private static final int BACKEND_COUNT = 4;

    /** Random source for backend selection. */
    private final Random random;

    /**
     * Constructs the roulette logger with a default random source.
     */
    public DependencyRouletteLogger() {
        this.random = new Random();
    }

    /**
     * Constructs the roulette logger with the provided random source,
     * enabling deterministic backend selection in tests.
     *
     * @param rng the random number generator for backend selection
     */
    DependencyRouletteLogger(final Random rng) {
        this.random = rng;
    }

    /**
     * Logs the given message to a randomly selected backend.
     * Backend selection is uniform over the four available sinks.
     *
     * @param message the message to log
     */
    public void log(final String message) {
        int backend = random.nextInt(BACKEND_COUNT);
        switch (backend) {
            case 0 -> JUL_LOG.log(Level.INFO, message);
            case 1 -> System.out.printf(
                    "[ROULETTE-OUT] %s%n", message);
            case 2 -> System.err.printf(
                    "[ROULETTE-ERR] %s%n", message);
            default -> SLF4J_LOG.info(
                    "[ROULETTE-SLF4J] {}", message);
        }
    }
}
