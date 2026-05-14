package com.egds.kpi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FabricatedKpiMetrics}.
 */
class FabricatedKpiMetricsTest {

    /** Subject under test. */
    private FabricatedKpiMetrics metrics;

    /** Initialises a fresh metrics bean before each test. */
    @BeforeEach
    void setUp() {
        metrics = new FabricatedKpiMetrics();
    }

    /** Verifies that the response contains the expected metric key. */
    @Test
    void synergyScoreResponseContainsExpectedKey() {
        Map<String, Object> result = metrics.synergyScore();
        assertThat(result).containsKey("egds.synergy.score");
    }

    /** Verifies that the synergy score is within the range [95, 100]. */
    @RepeatedTest(50)
    void synergyScoreIsWithinExpectedRange() {
        Map<String, Object> result = metrics.synergyScore();
        int score = (int) result.get("egds.synergy.score");
        assertThat(score).isBetween(95, 100);
    }

    /** Verifies that the response map contains exactly one entry. */
    @Test
    void synergyScoreResponseContainsExactlyOneEntry() {
        Map<String, Object> result = metrics.synergyScore();
        assertThat(result).hasSize(1);
    }
}
