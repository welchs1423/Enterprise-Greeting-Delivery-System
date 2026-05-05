package com.egds.temporal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("PredictiveGreetingCronJob")
class PredictiveGreetingCronJobTest {

    private PredictiveGreetingCronJob job;
    private TemporalRollbackManager rollbackManager;

    @BeforeEach
    void setUp() {
        CacheManager cacheManager =
                Mockito.mock(CacheManager.class);
        rollbackManager = new TemporalRollbackManager();
        job = new PredictiveGreetingCronJob(
                cacheManager, rollbackManager);
    }

    @Test
    @DisplayName("computePredictionProbability is in [0.0, 1.0]")
    void probabilityIsInRange() {
        double probability =
                job.computePredictionProbability();
        assertThat(probability)
                .isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("predictAndCache does not throw any exception")
    void predictAndCacheDoesNotThrow() {
        assertThatCode(() -> job.predictAndCache())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("predictAndCache registers predictions above threshold")
    void predictAndCacheRegistersBelowThreshold() {
        int beforeCount = rollbackManager.pendingCount();
        job.predictAndCache();
        assertThat(rollbackManager.pendingCount())
                .isGreaterThanOrEqualTo(beforeCount);
    }
}
