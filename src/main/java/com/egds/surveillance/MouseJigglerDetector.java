package com.egds.surveillance;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * HTTP filter that detects mouse-jiggler automation by analysing
 * per-IP request-interval regularity.
 *
 * <p>A sliding window of four consecutive timestamps is maintained per
 * client IP. If all three derived intervals deviate from the baseline
 * by no more than {@value #JITTER_TOLERANCE_MS} milliseconds, the
 * traffic pattern is classified as macro automation. The offending
 * client receives HTTP 429 and an HR escalation notice.
 *
 * <p>Set {@code egds.surveillance.jiggler.enabled=false} in test
 * properties to prevent timing-dependent false positives during
 * automated test suites.
 */
@Component
public class MouseJigglerDetector extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(MouseJigglerDetector.class);

    /**
     * Maximum deviation between consecutive intervals (ms) that still
     * qualifies as mechanically regular macro behaviour.
     */
    static final long JITTER_TOLERANCE_MS = 10L;

    /** Number of consecutive intervals evaluated per detection cycle. */
    private static final int REQUIRED_INTERVALS = 3;

    /** Number of timestamps retained per IP address in the sliding window. */
    private static final int TIMESTAMP_WINDOW = 4;

    /** HTTP 429 returned when macro activity is confirmed. */
    private static final int TOO_MANY_REQUESTS = 429;

    /** Content type for rejection responses (UTF-8 for Korean payload). */
    private static final String CONTENT_TYPE =
            "application/json;charset=UTF-8";

    /** Header inspected for the originating client IP. */
    private static final String FORWARDED_FOR = "X-Forwarded-For";

    /** Per-IP sliding window of recent request timestamps. */
    private final ConcurrentHashMap<String, Deque<Long>> ipTimestamps =
            new ConcurrentHashMap<>();

    /**
     * When false, the filter forwards all requests without timing analysis.
     * Controlled by {@code egds.surveillance.jiggler.enabled}.
     */
    @Value("${egds.surveillance.jiggler.enabled:true}")
    private boolean jigglerEnabled;

    /**
     * Evaluates the inter-request interval regularity for the client IP.
     * Returns HTTP 429 with HR escalation when macro activity is detected,
     * or forwards to the next filter otherwise.
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
        if (!isJigglerEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        String clientIp = resolveClientIp(request);
        if (isMacroPattern(clientIp, currentTimeMillis())) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                        "[SURVEILLANCE] Macro pattern from {}."
                        + " Escalating to HR.", clientIp);
            }
            response.setStatus(TOO_MANY_REQUESTS);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\"HR 팀에 보고되었습니다\""
                    + ",\"status\":429}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Records the current timestamp for the given IP and returns true
     * when the last {@value #REQUIRED_INTERVALS} intervals all fall
     * within {@value #JITTER_TOLERANCE_MS} ms of the baseline interval.
     *
     * @param clientIp the resolved client IP key
     * @param nowMs    the current wall-clock time in milliseconds
     * @return true if a macro-automation pattern has been detected
     */
    boolean isMacroPattern(final String clientIp, final long nowMs) {
        Deque<Long> timestamps = ipTimestamps.computeIfAbsent(
                clientIp, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            timestamps.addLast(nowMs);
            while (timestamps.size() > TIMESTAMP_WINDOW) {
                timestamps.removeFirst();
            }
            if (timestamps.size() < TIMESTAMP_WINDOW) {
                return false;
            }
            long[] intervals = extractIntervals(timestamps);
            long base = intervals[0];
            for (int i = 1; i < REQUIRED_INTERVALS; i++) {
                if (Math.abs(intervals[i] - base) > JITTER_TOLERANCE_MS) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Extracts consecutive inter-timestamp intervals from the window.
     *
     * @param timestamps the per-IP sliding window of recent timestamps
     * @return array of {@value #REQUIRED_INTERVALS} interval values in ms
     */
    private long[] extractIntervals(final Deque<Long> timestamps) {
        long[] intervals = new long[REQUIRED_INTERVALS];
        Iterator<Long> it = timestamps.iterator();
        long prev = it.next();
        for (int i = 0; i < REQUIRED_INTERVALS; i++) {
            long curr = it.next();
            intervals[i] = curr - prev;
            prev = curr;
        }
        return intervals;
    }

    /**
     * Returns the current wall-clock time in milliseconds.
     * Package-private to allow override in unit tests.
     *
     * @return current time in milliseconds
     */
    long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Returns true when jiggler detection is active.
     * Package-private to allow override in unit tests.
     *
     * @return true if mouse-jiggler detection is currently enabled
     */
    boolean isJigglerEnabled() {
        return jigglerEnabled;
    }

    /**
     * Resolves the originating client IP from the request.
     * Prefers the first value in {@code X-Forwarded-For} when present.
     *
     * @param request the incoming HTTP request
     * @return the resolved client IP string
     */
    String resolveClientIp(final HttpServletRequest request) {
        String forwarded = request.getHeader(FORWARDED_FOR);
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Exempts authentication and actuator endpoints from jiggler detection.
     *
     * @param request the incoming request
     * @return true if this filter should not apply to the request
     */
    @Override
    protected boolean shouldNotFilter(
            final HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/actuator/");
    }
}
