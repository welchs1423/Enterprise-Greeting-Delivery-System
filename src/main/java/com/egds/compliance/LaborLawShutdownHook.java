package com.egds.compliance;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Enforces a statutory weekly working-hours limit on the system's
 * cumulative uptime.
 *
 * <p>When the limit is exceeded, all Resilience4j circuit breakers
 * registered in the {@link CircuitBreakerRegistry} are forced into the
 * OPEN state and a {@link LaborLawViolationException} is thrown to
 * block further greeting delivery.
 *
 * <p>The default threshold is 52 hours per week. This value can be
 * overridden for testing via {@link #setThresholdHours(long)}.
 * Callers advance the uptime counter via {@link #recordUptime(long)}.
 */
@Component
public class LaborLawShutdownHook {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(LaborLawShutdownHook.class);

    /** Default maximum weekly uptime in hours. */
    private static final long DEFAULT_THRESHOLD_HOURS = 52L;

    /** Milliseconds per hour, used to convert ms counter to hours. */
    private static final long MS_PER_HOUR = 3_600_000L;

    /** Registry used to enumerate and force-open circuit breakers. */
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /** Running total of recorded uptime in milliseconds. */
    private final AtomicLong cumulativeUptimeMs = new AtomicLong(0);

    /** Configured maximum uptime threshold in hours. */
    private volatile long thresholdHours = DEFAULT_THRESHOLD_HOURS;

    /**
     * Constructs a {@code LaborLawShutdownHook} with the given circuit
     * breaker registry.
     *
     * @param registry Resilience4j registry used to enumerate and
     *                 force-open all registered circuit breakers
     */
    public LaborLawShutdownHook(
            final CircuitBreakerRegistry registry) {
        this.circuitBreakerRegistry = registry;
    }

    /**
     * Adds the given duration to the cumulative uptime counter and
     * enforces the statutory limit when the total crosses the threshold.
     *
     * @param uptimeMs additional uptime in milliseconds to record
     * @throws LaborLawViolationException if cumulative uptime in hours
     *         reaches or exceeds the configured threshold
     */
    public void recordUptime(final long uptimeMs) {
        long total = cumulativeUptimeMs.addAndGet(uptimeMs);
        long hours = total / MS_PER_HOUR;
        LOG.debug("[LABOR-LAW] cumulative={}ms ({}h)", total, hours);
        if (hours >= thresholdHours) {
            enforce(hours);
        }
    }

    /**
     * Forces all registered circuit breakers into the OPEN state and
     * raises a {@link LaborLawViolationException}.
     *
     * @param totalHours cumulative uptime hours that triggered enforcement
     */
    private void enforce(final long totalHours) {
        LOG.error(
                "[LABOR-LAW] 주 52시간 초과: cumulative={}h threshold={}h",
                totalHours, thresholdHours);
        circuitBreakerRegistry.getAllCircuitBreakers()
                .forEach(cb -> {
                    cb.transitionToOpenState();
                    LOG.warn(
                            "[LABOR-LAW] circuit breaker opened name={}",
                            cb.getName());
                });
        throw new LaborLawViolationException(totalHours, thresholdHours);
    }

    /**
     * Overrides the statutory uptime threshold.
     *
     * @param newThresholdHours new maximum uptime in hours
     */
    public void setThresholdHours(final long newThresholdHours) {
        this.thresholdHours = newThresholdHours;
    }

    /**
     * Returns the current cumulative uptime in milliseconds.
     *
     * @return cumulative uptime in milliseconds
     */
    public long getCumulativeUptimeMs() {
        return cumulativeUptimeMs.get();
    }
}
