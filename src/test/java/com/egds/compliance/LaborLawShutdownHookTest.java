package com.egds.compliance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LaborLawShutdownHook}.
 */
class LaborLawShutdownHookTest {

    /** Milliseconds per hour, used to compute threshold-crossing values. */
    private static final long MS_PER_HOUR = 3_600_000L;

    /** Registry shared between tests. */
    private CircuitBreakerRegistry registry;

    /** Hook under test, configured with a 1-hour threshold. */
    private LaborLawShutdownHook hook;

    /**
     * Creates a fresh registry and hook with a 1-hour threshold before
     * each test.
     */
    @BeforeEach
    void setUp() {
        registry = CircuitBreakerRegistry.ofDefaults();
        hook = new LaborLawShutdownHook(registry);
        hook.setThresholdHours(1L);
    }

    /**
     * Verifies that recording uptime below the threshold does not throw
     * and the cumulative counter is updated correctly.
     */
    @Test
    void recordUptime_belowThreshold_doesNotThrow() {
        hook.recordUptime(MS_PER_HOUR - 1L);
        assertThat(hook.getCumulativeUptimeMs())
                .isEqualTo(MS_PER_HOUR - 1L);
    }

    /**
     * Verifies that reaching the threshold throws
     * {@link LaborLawViolationException}.
     */
    @Test
    void recordUptime_atThreshold_throwsViolationException() {
        assertThatThrownBy(() -> hook.recordUptime(MS_PER_HOUR))
                .isInstanceOf(LaborLawViolationException.class);
    }

    /**
     * Verifies that the exception carries the correct uptime and
     * threshold values.
     */
    @Test
    void recordUptime_atThreshold_exceptionHasCorrectFields() {
        assertThatThrownBy(() -> hook.recordUptime(MS_PER_HOUR))
                .isInstanceOf(LaborLawViolationException.class)
                .satisfies(ex -> {
                    LaborLawViolationException e =
                            (LaborLawViolationException) ex;
                    assertThat(e.getCumulativeUptimeHours()).isEqualTo(1L);
                    assertThat(e.getThresholdHours()).isEqualTo(1L);
                });
    }

    /**
     * Verifies that all registered circuit breakers are forced OPEN
     * when the violation is triggered.
     */
    @Test
    void recordUptime_atThreshold_opensAllCircuitBreakers() {
        CircuitBreaker cb = registry.circuitBreaker("test-cb");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        assertThatThrownBy(() -> hook.recordUptime(MS_PER_HOUR))
                .isInstanceOf(LaborLawViolationException.class);

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
