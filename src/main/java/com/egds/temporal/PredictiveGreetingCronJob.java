package com.egds.temporal;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled component implementing Chronos Predictive Routing.
 *
 * <p>Every 60 seconds this job computes a probability score that
 * a greeting request will arrive in the next scheduling cycle.
 * When the score exceeds {@link #PREDICTION_THRESHOLD}, a
 * greeting is pre-generated and placed in the L1 cache under
 * {@link #CACHE_NAME} so that the subsequent HTTP request
 * receives a cache hit instead of triggering the full pipeline.
 *
 * <p>Each pre-generated entry is registered with
 * {@link TemporalRollbackManager}. Predictions that are not
 * claimed by a real request within the rollback window are
 * voided via compensating transaction on the next tick.
 */
@Component
public class PredictiveGreetingCronJob {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PredictiveGreetingCronJob.class);

    /** Probability threshold above which a greeting is cached. */
    static final double PREDICTION_THRESHOLD = 0.65;

    /** Spring cache region for pre-generated greetings. */
    static final String CACHE_NAME = "predictiveGreetings";

    private final CacheManager cacheManager;
    private final TemporalRollbackManager rollbackManager;

    /**
     * Constructs the cron job with its required collaborators.
     *
     * @param cacheManager    Spring cache manager for L1 storage
     * @param rollbackManager temporal rollback coordinator
     */
    public PredictiveGreetingCronJob(
            final CacheManager cacheManager,
            final TemporalRollbackManager rollbackManager) {
        this.cacheManager = cacheManager;
        this.rollbackManager = rollbackManager;
    }

    /**
     * Main prediction tick executed every 60 seconds.
     *
     * <p>First triggers expired-entry rollback, then computes the
     * probability for the current minute and conditionally
     * pre-caches a greeting and registers it for rollback
     * tracking.
     */
    @Scheduled(fixedRate = 60_000)
    public void predictAndCache() {
        rollbackManager.rollbackExpired();
        double probability = computePredictionProbability();
        log.info(
                "Chronos tick: probability={} threshold={}",
                String.format("%.4f", probability),
                PREDICTION_THRESHOLD);
        if (probability >= PREDICTION_THRESHOLD) {
            String id = UUID.randomUUID().toString();
            PredictedGreetingEntry entry =
                    new PredictedGreetingEntry(
                            id, "Hello, World!",
                            Instant.now(), false);
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.put(id, entry);
            }
            rollbackManager.register(entry);
            log.info(
                    "Chronos predict: correlationId={} cached",
                    id);
        }
    }

    /**
     * Computes a time-of-day-weighted probability in [0.0, 1.0].
     *
     * <p>The model uses a bimodal Gaussian distribution peaking
     * at 09:00 and 14:00, representing typical business-hour
     * greeting request patterns.
     *
     * @return predicted request probability for the current minute
     */
    double computePredictionProbability() {
        LocalTime now = LocalTime.now();
        double normalised =
                (now.getHour() * 60.0 + now.getMinute())
                        / 1440.0;
        double peak1 = gaussian(normalised, 9.0 / 24.0, 0.05);
        double peak2 = gaussian(normalised, 14.0 / 24.0, 0.05);
        return Math.min(1.0, peak1 + peak2);
    }

    private static double gaussian(
            final double x,
            final double mean,
            final double sigma) {
        double diff = x - mean;
        return Math.exp(
                -(diff * diff) / (2.0 * sigma * sigma));
    }
}
