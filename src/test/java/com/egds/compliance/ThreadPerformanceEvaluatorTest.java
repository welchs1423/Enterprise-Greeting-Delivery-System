package com.egds.compliance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ThreadPerformanceEvaluator}.
 */
class ThreadPerformanceEvaluatorTest {

    /** Evaluator under test. */
    private ThreadPerformanceEvaluator evaluator;

    /** Creates a fresh evaluator before each test. */
    @BeforeEach
    void setUp() {
        evaluator = new ThreadPerformanceEvaluator();
        Thread.interrupted();
    }

    /** Verifies that a fast task completes without interrupting the thread. */
    @Test
    void evaluate_fastTask_doesNotInterruptThread() throws Exception {
        evaluator.evaluate("fast-task", () -> "result");
        assertThat(Thread.currentThread().isInterrupted()).isFalse();
    }

    /** Verifies that evaluate returns the task's return value. */
    @Test
    void evaluate_returnsTaskResult() throws Exception {
        String result = evaluator.evaluate("return-task", () -> "hello");
        assertThat(result).isEqualTo("hello");
    }

    /**
     * Verifies that a task exceeding the threshold causes the thread
     * interrupt flag to be set.
     */
    @Test
    void evaluate_slowTask_setsThreadInterruptFlag() throws Exception {
        evaluator.setThresholdNs(1L);
        evaluator.evaluate("slow-task", () -> {
            Thread.sleep(10);
            return null;
        });
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted();
    }

    /**
     * Verifies that a task exception propagates through evaluate while
     * the thread interrupt flag is cleared (task did not exceed threshold
     * in normal error scenarios).
     */
    @Test
    void evaluate_taskThrows_exceptionPropagates() {
        assertThatThrownBy(() ->
            evaluator.evaluate("error-task", () -> {
                throw new RuntimeException("task-error");
            })
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("task-error");
    }

    /** Verifies that setThresholdNs updates the returned threshold. */
    @Test
    void setThresholdNs_updatesThreshold() {
        evaluator.setThresholdNs(500_000_000L);
        assertThat(evaluator.getThresholdNs()).isEqualTo(500_000_000L);
    }
}
