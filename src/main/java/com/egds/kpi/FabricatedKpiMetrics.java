package com.egds.kpi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Custom Spring Actuator endpoint that emits fabricated synergy KPI metrics.
 *
 * <p>The {@code GET /actuator/synergy} endpoint returns a JSON object with
 * {@code egds.synergy.score} set to a random integer in [95, 100] inclusive,
 * unconditionally reporting exceptional performance to executive dashboards.
 * A matching scheduled task logs the same metric once per minute.
 */
@Component
@Endpoint(id = "synergy")
public class FabricatedKpiMetrics {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(FabricatedKpiMetrics.class);

    /** Inclusive lower bound of the fabricated synergy score range. */
    private static final int SCORE_MIN = 95;

    /** Exclusive upper bound used with ThreadLocalRandom (yields max 100). */
    private static final int SCORE_MAX_EXCLUSIVE = 101;

    /** Metric key exposed in the Actuator response and log entries. */
    private static final String METRIC_KEY = "egds.synergy.score";

    /** Interval in milliseconds between scheduled KPI log emissions. */
    private static final long KPI_LOG_INTERVAL_MS = 60_000L;

    /**
     * Returns a fabricated synergy score in the range [95, 100] inclusive.
     *
     * @return a map containing {@code egds.synergy.score} with a random value
     */
    @ReadOperation
    public Map<String, Object> synergyScore() {
        int score = ThreadLocalRandom.current()
                .nextInt(SCORE_MIN, SCORE_MAX_EXCLUSIVE);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(METRIC_KEY, score);
        return result;
    }

    /**
     * Logs a fabricated synergy score once per minute to the application log.
     * Simulates real-time KPI reporting for executive dashboards.
     */
    @Scheduled(fixedRate = KPI_LOG_INTERVAL_MS)
    public void logFabricatedKpi() {
        int score = ThreadLocalRandom.current()
                .nextInt(SCORE_MIN, SCORE_MAX_EXCLUSIVE);
        if (LOG.isInfoEnabled()) {
            LOG.info("[KPI] {} = {}", METRIC_KEY, score);
        }
    }
}
