package com.egds.rto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * HTTP filter that enforces a Return-to-Office (RTO) geofencing policy.
 *
 * <p>Requests whose resolved client IP is not {@code 127.0.0.1} are
 * rejected with HTTP 403 and a JSON body containing the message
 * "Please return to the office". The client IP is read from the
 * {@code X-Forwarded-For} header when present, falling back to
 * {@link HttpServletRequest#getRemoteAddr()}.
 *
 * <p>Authentication and actuator paths bypass this filter.
 * Set {@code egds.rto.enabled=false} in test properties to prevent
 * blanket 403 rejections during automated tests.
 */
@Component
public class RtoGeofenceFilter extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(RtoGeofenceFilter.class);

    /** The only IP address considered within the office perimeter. */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String OFFICE_IP = "127.0.0.1";

    /** HTTP status code returned for out-of-office access attempts. */
    private static final int FORBIDDEN_STATUS = 403;

    /** Content type for rejection error responses. */
    private static final String CONTENT_TYPE = "application/json";

    /** Request header inspected for the originating client IP. */
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    /**
     * When false, the filter forwards all requests without IP inspection.
     * Controlled by {@code egds.rto.enabled}.
     */
    @Value("${egds.rto.enabled:true}")
    private boolean rtoEnabled;

    /**
     * Inspects the client IP and either forwards the request or returns
     * HTTP 403 when the IP does not match the office perimeter.
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
        if (!isRtoEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        String clientIp = resolveClientIp(request);
        if (!OFFICE_IP.equals(clientIp)) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                        "[RTO] Remote access attempt from {}. "
                        + "Request rejected.", clientIp);
            }
            response.setStatus(FORBIDDEN_STATUS);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\"Please return to the office\""
                    + ",\"status\":403}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns true when RTO enforcement is active.
     * Package-private to allow override in unit tests.
     *
     * @return true if RTO geofencing is currently enabled
     */
    boolean isRtoEnabled() {
        return rtoEnabled;
    }

    /**
     * Resolves the originating client IP from the request.
     * Prefers the first value in {@code X-Forwarded-For} when present.
     *
     * @param request the incoming HTTP request
     * @return the resolved client IP string
     */
    String resolveClientIp(final HttpServletRequest request) {
        String forwarded = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Exempts authentication and actuator endpoints from RTO enforcement.
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
