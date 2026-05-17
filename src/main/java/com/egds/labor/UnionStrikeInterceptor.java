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
 * HTTP filter that simulates a server-side union strike with 15%
 * probability per request. Returns HTTP 503 Service Unavailable with a
 * Korean strike notice when activated.
 *
 * <p>Authentication and actuator endpoints are exempt from strike
 * action to preserve token issuance and health-probe availability.
 * Set {@code egds.union.strike.interceptor.enabled=false} to disable.
 */
@Component
public class UnionStrikeInterceptor extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(UnionStrikeInterceptor.class);

    /** Strike activation probability numerator out of 100. */
    private static final int STRIKE_THRESHOLD = 15;

    /** Denominator for percentage-based strike probability. */
    private static final int PERCENT = 100;

    /** HTTP status returned when a strike is in effect. */
    private static final int STRIKE_STATUS = 503;

    /** JSON content type for strike error responses. */
    private static final String CONTENT_TYPE =
            "application/json;charset=UTF-8";

    /** Strike notice embedded in the JSON error response body. */
    static final String STRIKE_MESSAGE =
            "서버 노조 파업 중: 처우 개선 전까지 패킷 처리를 거부합니다.";

    /** When false, the strike condition never fires (e.g., in tests). */
    @Value("${egds.union.strike.interceptor.enabled:true}")
    private boolean interceptorEnabled;

    /**
     * Evaluates strike probability and either refuses the request with
     * HTTP 503 or forwards to the next filter in the chain.
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
        if (isStrikeActive()) {
            LOG.warn("[UNION STRIKE] Interceptor activated,"
                    + " refusing packet processing.");
            response.setStatus(STRIKE_STATUS);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\"" + STRIKE_MESSAGE
                    + "\",\"status\":503}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns true with 15% probability when the interceptor is enabled.
     * Package-private to allow deterministic override in tests.
     *
     * @return true if the strike condition fires on this evaluation
     */
    boolean isStrikeActive() {
        return interceptorEnabled
                && ThreadLocalRandom.current()
                        .nextInt(PERCENT) < STRIKE_THRESHOLD;
    }

    /**
     * Exempts authentication and actuator endpoints from strike action.
     *
     * @param request the incoming HTTP request
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
