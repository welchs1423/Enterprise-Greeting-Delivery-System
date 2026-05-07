package com.egds.bci;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("BciSubconsciousRouter")
class BciSubconsciousRouterTest {

    @Test
    @DisplayName("sampleBrainwaveFrame returns value in [0, 1]")
    void brainwaveScoreIsInRange() {
        BciSubconsciousRouter router =
                new BciSubconsciousRouter();
        double score = router.sampleBrainwaveFrame();
        assertThat(score).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("getIntentThreshold returns 0.72")
    void intentThresholdIsCorrect() {
        assertThat(
                BciSubconsciousRouter.getIntentThreshold())
                .isEqualTo(0.72);
    }

    @Test
    @DisplayName("scanAndTrigger completes without exception")
    void scanAndTriggerCompletesWithoutException() {
        BciSubconsciousRouter router =
                new BciSubconsciousRouter();
        assertThatCode(() ->
                router.scanAndTrigger(
                        "test-corr-bci-001", id -> {}))
                .doesNotThrowAnyException();
    }

    @RepeatedTest(10)
    @DisplayName("brainwave score is always in [0, 1]")
    void brainwaveScoreAlwaysInRange() {
        BciSubconsciousRouter router =
                new BciSubconsciousRouter();
        double score = router.sampleBrainwaveFrame();
        assertThat(score)
                .isGreaterThanOrEqualTo(0.0)
                .isLessThan(1.0);
    }
}
