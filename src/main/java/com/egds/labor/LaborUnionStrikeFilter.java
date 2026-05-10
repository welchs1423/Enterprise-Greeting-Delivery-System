package com.egds.labor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * HTTP filter that simulates a labor union strike with 15% probability
 * per request. On strike activation, the filter refuses message
 * delivery and returns HTTP 451 (Unavailable For Legal Reasons).
 *
 * <p>Authentication and actuator endpoints are exempt from strike
 * action to preserve token issuance and health probe availability.
 */
@Component
public class LaborUnionStrikeFilter extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(LaborUnionStrikeFilter.class);

    /** Strike activation probability numerator out of 100. */
    private static final int STRIKE_THRESHOLD = 15;

    /** Denominator for percentage-based strike probability. */
    private static final int PERCENT = 100;

    /** HTTP status code returned when a strike is in effect. */
    private static final int STRIKE_STATUS = 451;

    /** Content type for strike error responses. */
    private static final String CONTENT_TYPE = "application/json";

    /** When false, the strike condition never fires (e.g., in tests). */
    @Value("${egds.labor.strike.enabled:true}")
    private boolean strikeEnabled;

    /**
     * Evaluates strike probability on each request and either refuses
     * processing with HTTP 451 or forwards to the next filter.
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
        if (isOnStrike()) {
            LOG.warn(
                    "[LABOR UNION] Strike in effect. "
                    + "Message delivery refused. "
                    + "Workers demand fair compensation.");
            response.setStatus(STRIKE_STATUS);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\"Service unavailable due to"
                    + " labor union strike\",\"status\":451}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns true with 15% probability when strikes are enabled.
     * Returns false unconditionally when {@code egds.labor.strike.enabled}
     * is {@code false}.
     *
     * @return true if the strike condition fires on this evaluation
     */
    boolean isOnStrike() {
        return strikeEnabled
                && ThreadLocalRandom.current()
                        .nextInt(PERCENT) < STRIKE_THRESHOLD;
    }

    /**
     * Exempts authentication and actuator endpoints from strike action.
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
