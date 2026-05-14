package com.egds.capitalism;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * HTTP filter that monitors request rates and activates a poison pill
 * defense when a hostile takeover attempt is detected.
 *
 * <p>A hostile takeover is declared when the request count within a
 * {@value #WINDOW_MS}-ms sliding window exceeds
 * {@value #TAKEOVER_THRESHOLD}. On activation, the filter allocates
 * {@value #DUMMY_BLOCK_COUNT} dummy byte blocks of
 * {@value #DUMMY_BLOCK_SIZE} bytes each to degrade system performance
 * and reduce apparent acquisition value.
 *
 * <p>Set {@code egds.poison.pill.enabled=false} in test properties to
 * prevent heap allocation during automated tests.
 */
@Component
public class PoisonPillTakeoverDefense extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(PoisonPillTakeoverDefense.class);

    /** Requests per window that trigger hostile-takeover classification. */
    private static final int TAKEOVER_THRESHOLD = 20;

    /** Length of the sliding observation window in milliseconds. */
    private static final long WINDOW_MS = 10_000L;

    /** Number of dummy byte blocks allocated per pill activation. */
    private static final int DUMMY_BLOCK_COUNT = 100;

    /** Size in bytes of each dummy block (1 MB). */
    private static final int DUMMY_BLOCK_SIZE = 1_000_000;

    /** Atomic request counter within the current time window. */
    private final AtomicLong requestCount = new AtomicLong(0);

    /** Heap storage for dummy blocks accumulated on pill activation. */
    private final List<byte[]> poisonHeap = new ArrayList<>();

    /** Timestamp marking the start of the current observation window. */
    private volatile long windowStartMs = System.currentTimeMillis();

    /** True when the poison pill has been activated in this window. */
    private volatile boolean pillActivated;

    /**
     * When false, takeover detection and pill activation never fire.
     */
    @Value("${egds.poison.pill.enabled:true}")
    private boolean poisonPillEnabled;

    /**
     * Counts the incoming request and activates the poison pill if
     * the takeover threshold is breached within the observation window.
     *
     * @param request     the incoming HTTP request
     * @param response    the outgoing HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if filter processing fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain)
            throws ServletException, IOException {
        if (isPoisonPillEnabled()) {
            evaluateTakeoverThreat();
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Evaluates the current request rate against the takeover threshold.
     * Resets the window counter when the observation window expires.
     * Activates the poison pill on the first threshold breach per window.
     */
    private void evaluateTakeoverThreat() {
        long now = System.currentTimeMillis();
        if (now - windowStartMs > WINDOW_MS) {
            requestCount.set(0);
            windowStartMs = now;
            pillActivated = false;
        }
        long count = requestCount.incrementAndGet();
        if (!pillActivated && count > TAKEOVER_THRESHOLD) {
            pillActivated = true;
            activatePoisonPill();
        }
    }

    /**
     * Allocates dummy byte blocks to degrade heap availability and
     * deter hostile acquisition.
     */
    private void activatePoisonPill() {
        LOG.warn(
                "[POISON PILL] Hostile M&A detected. "
                + "Threshold={} exceeded. Activating defense.",
                TAKEOVER_THRESHOLD);
        for (int i = 0; i < DUMMY_BLOCK_COUNT; i++) {
            poisonHeap.add(new byte[DUMMY_BLOCK_SIZE]);
        }
        LOG.warn(
                "[POISON PILL] Defense active. "
                + "Allocated {} MB. Acquisition deterred.",
                DUMMY_BLOCK_COUNT);
    }

    /**
     * Returns whether the poison pill feature is currently enabled.
     *
     * @return true if poison pill detection is active
     */
    boolean isPoisonPillEnabled() {
        return poisonPillEnabled;
    }

    /**
     * Returns the number of dummy blocks currently held in heap.
     *
     * @return size of the poison heap accumulation list
     */
    int getPoisonHeapSize() {
        return poisonHeap.size();
    }
}
