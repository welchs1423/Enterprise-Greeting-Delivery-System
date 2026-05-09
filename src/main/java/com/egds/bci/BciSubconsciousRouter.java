package com.egds.bci;

import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Brain-computer interface subconscious router (simulation).
 *
 * <p>Simulates alpha-band (8-12 Hz) and beta-band (12-30 Hz)
 * brainwave stream analysis. When the derived intent score
 * exceeds the detection threshold, an event is scheduled to
 * fire 500 ms before the user's expected explicit request.
 */
@Component
public class BciSubconsciousRouter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(
                    BciSubconsciousRouter.class);

    /** Minimum alpha-band frequency in Hz. */
    private static final double ALPHA_MIN_HZ = 8.0;

    /** Maximum alpha-band frequency in Hz. */
    private static final double ALPHA_MAX_HZ = 12.0;

    /** Minimum beta-band frequency in Hz. */
    private static final double BETA_MIN_HZ = 12.0;

    /** Maximum beta-band frequency in Hz. */
    private static final double BETA_MAX_HZ = 30.0;

    /** Milliseconds to fire event before explicit intent. */
    private static final long PRECOGNITION_DELAY_MS = 500L;

    /** Intent score threshold for subconscious detection. */
    private static final double INTENT_THRESHOLD = 0.72;

    /** Cryptographically secure random source. */
    private final SecureRandom random = new SecureRandom();

    /** Daemon scheduler for pre-emptive event dispatch. */
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "bci-router");
                t.setDaemon(true);
                return t;
            });

    /**
     * Samples a synthetic brainwave frame and derives an
     * intent score from the beta-to-total-power ratio.
     *
     * @return intent score in the range [0.0, 1.0)
     */
    public double sampleBrainwaveFrame() {
        double alpha = ALPHA_MIN_HZ
                + random.nextDouble()
                * (ALPHA_MAX_HZ - ALPHA_MIN_HZ);
        double beta = BETA_MIN_HZ
                + random.nextDouble()
                * (BETA_MAX_HZ - BETA_MIN_HZ);
        double score = beta / (alpha + beta);
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "BCI frame alpha={} Hz beta={} Hz"
                            + " intent={}",
                    String.format("%.2f", alpha),
                    String.format("%.2f", beta),
                    String.format("%.4f", score));
        }
        return score;
    }

    /**
     * Scans the subconscious stream and, when intent exceeds
     * the threshold, schedules the callback to fire
     * {@value #PRECOGNITION_DELAY_MS} ms before the expected
     * explicit request.
     *
     * @param correlationId       the request correlation ID
     * @param onSubconsciousIntent callback invoked on detection
     */
    public void scanAndTrigger(
            final String correlationId,
            final Consumer<String> onSubconsciousIntent) {
        double score = sampleBrainwaveFrame();
        if (score >= INTENT_THRESHOLD) {
            if (LOG.isInfoEnabled()) {
                LOG.info(
                        "BCI subconscious intent detected "
                                + "correlationId={} score={}"
                                + " schedulingIn={}ms",
                        correlationId,
                        String.format("%.4f", score),
                        PRECOGNITION_DELAY_MS);
            }
            scheduler.schedule(
                    () -> onSubconsciousIntent.accept(
                            correlationId),
                    PRECOGNITION_DELAY_MS,
                    TimeUnit.MILLISECONDS);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "BCI intent below threshold "
                                + "correlationId={} score={}",
                        correlationId,
                        String.format("%.4f", score));
            }
        }
    }

    /**
     * Returns the configured intent detection threshold.
     *
     * @return the threshold value in [0.0, 1.0]
     */
    public static double getIntentThreshold() {
        return INTENT_THRESHOLD;
    }
}
