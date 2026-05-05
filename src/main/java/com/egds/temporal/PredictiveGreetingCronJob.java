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
 * <p>Every {@value #FIXED_RATE_MS} milliseconds this job computes
 * a probability score that a greeting request will arrive in the
 * next cycle. When the score exceeds {@link #PREDICTION_THRESHOLD},
 * a greeting is pre-generated and placed in the L1 cache under
 * {@link #CACHE_NAME} so that the subsequent HTTP request receives
 * a hit rather than triggering the full pipeline.
 *
 * <p>Each pre-generated entry is registered with
 * {@link TemporalRollbackManager}. Predictions that are not
 * claimed by a real request within the rollback window are
 * voided via compensating transaction on the next tick.
 */
@Component
public class PredictiveGreetingCronJob {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(
                    PredictiveGreetingCronJob.class);

    /** Scheduler firing interval in milliseconds. */
    private static final long FIXED_RATE_MS = 60_000L;

    /** Probability threshold above which a greeting is cached. */
    static final double PREDICTION_THRESHOLD = 0.65;

    /** Spring cache region for pre-generated greetings. */
    static final String CACHE_NAME = "predictiveGreetings";

    /** Minutes per hour, used to normalise time-of-day. */
    private static final double MINS_PER_HOUR = 60.0;

    /** Total minutes per day, used as the normalisation divisor. */
    private static final double MINS_PER_DAY = 1440.0;

    /** Hour of day for the morning request-rate peak. */
    private static final double MORNING_PEAK_HOUR = 9.0;

    /** Hour of day for the afternoon request-rate peak. */
    private static final double AFTERNOON_PEAK_HOUR = 14.0;

    /** Hours per day, used to convert peak hours to fractions. */
    private static final double HOURS_PER_DAY = 24.0;

    /** Standard deviation (sigma) of each Gaussian peak. */
    private static final double GAUSSIAN_SIGMA = 0.05;

    /** Spring cache manager used for L1 storage. */
    private final CacheManager cacheManager;

    /** Temporal rollback coordinator. */
    private final TemporalRollbackManager rollbackManager;

    /**
     * Constructs the cron job with its required collaborators.
     *
     * @param cacheManagerBean    Spring cache manager for L1 storage
     * @param rollbackManagerBean temporal rollback coordinator
     */
    public PredictiveGreetingCronJob(
            final CacheManager cacheManagerBean,
            final TemporalRollbackManager rollbackManagerBean) {
        this.cacheManager = cacheManagerBean;
        this.rollbackManager = rollbackManagerBean;
    }

    /**
     * Main prediction tick executed every {@value #FIXED_RATE_MS} ms.
     *
     * <p>First triggers expired-entry rollback, then computes the
     * probability for the current minute and conditionally
     * pre-caches a greeting and registers it for rollback tracking.
     */
    @Scheduled(fixedRate = FIXED_RATE_MS)
    public void predictAndCache() {
        rollbackManager.rollbackExpired();
        double probability = computePredictionProbability();
        LOG.info(
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
            LOG.info(
                    "Chronos predict: correlationId={} cached",
                    id);
        }
    }

    /**
     * Computes a time-of-day-weighted probability in [0.0, 1.0].
     *
     * <p>The model uses a bimodal Gaussian distribution peaking
     * at {@value #MORNING_PEAK_HOUR}:00 and
     * {@value #AFTERNOON_PEAK_HOUR}:00, representing typical
     * business-hour greeting request patterns.
     *
     * @return predicted request probability for the current minute
     */
    double computePredictionProbability() {
        LocalTime now = LocalTime.now();
        double normalised =
                (now.getHour() * MINS_PER_HOUR + now.getMinute())
                        / MINS_PER_DAY;
        double peak1 = gaussian(
                normalised,
                MORNING_PEAK_HOUR / HOURS_PER_DAY,
                GAUSSIAN_SIGMA);
        double peak2 = gaussian(
                normalised,
                AFTERNOON_PEAK_HOUR / HOURS_PER_DAY,
                GAUSSIAN_SIGMA);
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
