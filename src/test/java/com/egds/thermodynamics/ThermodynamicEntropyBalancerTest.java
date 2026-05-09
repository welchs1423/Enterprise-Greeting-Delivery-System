package com.egds.thermodynamics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class ThermodynamicEntropyBalancerTest {

    private SmartHvacAdapter hvacAdapter;
    private ThermodynamicEntropyBalancer balancer;

    @BeforeEach
    void setUp() {
        hvacAdapter = Mockito.mock(SmartHvacAdapter.class);
        balancer = new ThermodynamicEntropyBalancer(hvacAdapter);
    }

    @Test
    void computeShannonEntropy_uniformDistribution_returnsLog2N() {
        // "abcd": 4 distinct chars p=0.25 each, H = log2(4) = 2.0
        double entropy = balancer.computeShannonEntropy("abcd");
        assertThat(entropy).isCloseTo(2.0, within(1e-10));
    }

    @Test
    void computeShannonEntropy_singleCharRepeated_returnsZero() {
        double entropy = balancer.computeShannonEntropy("aaaa");
        assertThat(entropy).isCloseTo(0.0, within(1e-10));
    }

    @Test
    void computeShannonEntropy_nullInput_returnsZero() {
        assertThat(balancer.computeShannonEntropy(null)).isEqualTo(0.0);
    }

    @Test
    void computeShannonEntropy_emptyInput_returnsZero() {
        assertThat(balancer.computeShannonEntropy("")).isEqualTo(0.0);
    }

    @Test
    void balanceEntropy_helloWorld_invokesHvacWithPositiveDelta() {
        String correlationId = "test-corr-1";
        double entropy = balancer.balanceEntropy(
                correlationId, "Hello, World!");
        assertThat(entropy).isGreaterThan(0.0);
        verify(hvacAdapter).requestCoolingOffset(
                eq(correlationId), anyDouble());
    }

    @Test
    void balanceEntropy_returnsComputedEntropyValue() {
        double returned = balancer.balanceEntropy("corr-2", "abcd");
        assertThat(returned).isCloseTo(2.0, within(1e-10));
    }
}
