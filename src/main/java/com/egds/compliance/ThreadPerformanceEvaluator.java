package com.egds.compliance;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Evaluates the wall-clock performance of pipeline threads in
 * nanosecond resolution.
 *
 * <p>Threads whose elapsed time exceeds the configured threshold are
 * flagged in the log as "권고사직 처리됨" and receive a thread interrupt
 * signal via {@link Thread#interrupt()}.
 *
 * <p>The threshold defaults to one second (1 000 000 000 ns) and can
 * be overridden per test via {@link #setThresholdNs(long)}.
 */
@Component
public class ThreadPerformanceEvaluator {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(ThreadPerformanceEvaluator.class);

    /** Default performance threshold: 1 second in nanoseconds. */
    private static final long DEFAULT_THRESHOLD_NS = 1_000_000_000L;

    /** Current performance threshold in nanoseconds. */
    private long thresholdNs = DEFAULT_THRESHOLD_NS;

    /**
     * Executes the supplied task while measuring elapsed nanoseconds.
     * If the elapsed time exceeds the configured threshold, the
     * executing thread is logged as terminated and interrupted.
     *
     * @param <T>      the return type of the task
     * @param taskName human-readable name used in log messages
     * @param task     the callable task to evaluate
     * @return the value returned by the task
     * @throws Exception if the task itself throws
     */
    public <T> T evaluate(
            final String taskName,
            final Callable<T> task) throws Exception {
        long start = System.nanoTime();
        try {
            return task.call();
        } finally {
            long elapsed = System.nanoTime() - start;
            assess(taskName, elapsed);
        }
    }

    /**
     * Assesses the current thread's performance for a completed task
     * and interrupts it if the threshold was exceeded.
     *
     * @param taskName  human-readable task identifier for log output
     * @param elapsedNs measured elapsed time in nanoseconds
     */
    private void assess(final String taskName, final long elapsedNs) {
        Thread thread = Thread.currentThread();
        LOG.debug(
                "[LABOR] task={} thread={} elapsed={}ns",
                taskName, thread.getName(), elapsedNs);
        if (elapsedNs > thresholdNs) {
            LOG.warn(
                    "[LABOR] 권고사직 처리됨 thread={} task={} elapsed={}ns",
                    thread.getName(), taskName, elapsedNs);
            thread.interrupt();
        }
    }

    /**
     * Overrides the performance threshold used for thread evaluation.
     *
     * @param newThresholdNs new threshold in nanoseconds
     */
    public void setThresholdNs(final long newThresholdNs) {
        this.thresholdNs = newThresholdNs;
    }

    /**
     * Returns the current performance threshold in nanoseconds.
     *
     * @return threshold in nanoseconds
     */
    public long getThresholdNs() {
        return thresholdNs;
    }
}
