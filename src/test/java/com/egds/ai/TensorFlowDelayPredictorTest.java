package com.egds.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TensorFlowDelayPredictor}.
 *
 * <p>The maximum predicted delay is overridden to 100 ms via
 * {@link ReflectionTestUtils} to allow fast iteration across multiple
 * inference samples without binding test runtime to the production
 * 5 000 ms ceiling.
 */
class TensorFlowDelayPredictorTest {

    private TensorFlowDelayPredictor predictor;

    @BeforeEach
    void setUp() {
        predictor = new TensorFlowDelayPredictor();
        ReflectionTestUtils.setField(predictor, "maxPredictedDelayMs", 100L);
    }

    @Test
    @Timeout(2)
    void applyPredictedDelay_doesNotThrow() throws InterruptedException {
        predictor.applyPredictedDelay();
    }

    @RepeatedTest(20)
    @Timeout(5)
    void applyPredictedDelay_repeatedInvocations_completeCleanly() {
        assertDoesNotThrow(() -> predictor.applyPredictedDelay());
    }

    @Test
    void predictDelayMs_resultWithinBounds() {
        for (int i = 0; i < 50; i++) {
            long predicted = predictor.predictDelayMs();
            assertTrue(predicted >= 0,
                    "Predicted delay must be non-negative");
            assertTrue(predicted <= 100,
                    "Predicted delay must not exceed maxPredictedDelayMs");
        }
    }

    @Test
    void predictDelayMs_zeroMaxDelay_alwaysReturnsZero() {
        ReflectionTestUtils.setField(predictor, "maxPredictedDelayMs", 0L);
        for (int i = 0; i < 20; i++) {
            long predicted = predictor.predictDelayMs();
            assertTrue(predicted == 0,
                    "With maxPredictedDelayMs=0, prediction must always be 0");
        }
    }

    @Test
    @Timeout(1)
    void applyPredictedDelay_zeroMaxDelay_returnsImmediately()
            throws InterruptedException {
        ReflectionTestUtils.setField(predictor, "maxPredictedDelayMs", 0L);
        predictor.applyPredictedDelay();
    }
}
