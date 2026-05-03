package com.egds.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for {@link QuantumDelayService}.
 *
 * <p>The max delay is overridden to 50 ms via {@link ReflectionTestUtils}
 * so that probabilistic branches can be exercised quickly without
 * coupling test runtime to the production 10 000 ms ceiling.
 */
class QuantumDelayServiceTest {

    private QuantumDelayService service;

    @BeforeEach
    void setUp() {
        service = new QuantumDelayService();
        ReflectionTestUtils.setField(service, "maxDelayMs", 50L);
    }

    @Test
    @Timeout(2)
    void applyQuantumDelay_doesNotThrow() throws InterruptedException {
        service.applyQuantumDelay();
    }

    /**
     * Runs 20 iterations to cover both the immediate and delayed eigenstates
     * with high statistical confidence.
     */
    @RepeatedTest(20)
    @Timeout(5)
    void applyQuantumDelay_bothEigenstatesCompleteCleanly() {
        assertDoesNotThrow(() -> service.applyQuantumDelay());
    }

    @Test
    @Timeout(1)
    void applyQuantumDelay_zeroMaxDelay_returnsImmediately()
            throws InterruptedException {
        ReflectionTestUtils.setField(service, "maxDelayMs", 0L);
        service.applyQuantumDelay();
    }
}
