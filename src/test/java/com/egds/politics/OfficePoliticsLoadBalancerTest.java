package com.egds.politics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OfficePoliticsLoadBalancer}.
 */
class OfficePoliticsLoadBalancerTest {

    private OfficePoliticsLoadBalancer balancer;

    /** Initialises a fresh load balancer before each test. */
    @BeforeEach
    void setUp() {
        balancer = new OfficePoliticsLoadBalancer();
    }

    /** Verifies that all subsequent route calls return the same node. */
    @Test
    void routeAlwaysReturnsElectedWorker() {
        String elected = balancer.route();
        for (int i = 0; i < 100; i++) {
            assertThat(balancer.route()).isEqualTo(elected);
        }
    }

    /** Verifies that the elected worker has the highest power score. */
    @Test
    void electedWorkerHasHighestPoliticalPower() {
        String elected = balancer.route();
        Map<String, Integer> powerMap = balancer.getWorkerPowerMap();
        int electedPower = powerMap.get(elected);
        powerMap.values().forEach(power ->
                assertThat(electedPower).isGreaterThanOrEqualTo(power));
    }

    /** Verifies that the worker pool is initialised with five nodes. */
    @Test
    void workerMapContainsFiveNodes() {
        assertThat(balancer.getWorkerPowerMap()).hasSize(5);
    }

    /** Verifies that all assigned power scores are positive. */
    @Test
    void allPowerScoresArePositive() {
        balancer.getWorkerPowerMap().values().forEach(power ->
                assertThat(power).isPositive());
    }

    /** Verifies that the power map is unmodifiable. */
    @Test
    void powerMapIsUnmodifiable() {
        Map<String, Integer> powerMap = balancer.getWorkerPowerMap();
        assertThat(powerMap).isNotNull();
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> powerMap.put("worker-99", 99));
    }
}
