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
 * HTTP filter enforcing the mandatory Return-to-Office (RTO) policy.
 *
 * <p>Requests whose resolved client IP does not fall within the
 * corporate intranet ({@code 10.50.0.0/16}) are rejected with
 * HTTP 403 and the directive "Return To Office." Requests carrying
 * the {@code X-Bypass-Rto} header are granted temporary passage
 * for infrastructure and integration testing purposes.
 *
 * <p>Authentication and actuator paths bypass this filter via
 * {@link #shouldNotFilter(HttpServletRequest)}.
 * Set {@code egds.rto.enforcement.enabled=false} in test properties
 * to prevent blanket 403 rejections during automated tests.
 */
@Component
public class RtoEnforcementFilter extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(RtoEnforcementFilter.class);

    /** HTTP 403 status code returned for out-of-office access. */
    private static final int FORBIDDEN = 403;

    /** Content type for rejection responses. */
    private static final String CONTENT_TYPE = "application/json";

    /** Header inspected for the originating client IP. */
    private static final String FORWARDED_FOR = "X-Forwarded-For";

    /** Header that grants bypass access for testing and infra use. */
    private static final String BYPASS_HEADER = "X-Bypass-Rto";

    /** Corporate intranet subnet prefix: 10.50.0.0/16. */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String INTRANET_PREFIX = "10.50.";

    /** Loopback IPv4 address accepted as intranet. */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String LOOPBACK_V4 = "127.0.0.1";

    /** Loopback IPv6 address accepted as intranet. */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String LOOPBACK_V6 = "::1";

    /**
     * When false, the filter forwards all requests without IP inspection.
     * Controlled by {@code egds.rto.enforcement.enabled}.
     */
    @Value("${egds.rto.enforcement.enabled:true}")
    private boolean enforcementEnabled;

    /**
     * Inspects the client IP and either forwards the request or returns
     * HTTP 403 when the IP is outside the corporate intranet.
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
        if (!isEnforcementEnabled()
                || request.getHeader(BYPASS_HEADER) != null) {
            filterChain.doFilter(request, response);
            return;
        }
        String clientIp = resolveClientIp(request);
        if (!isIntranetIp(clientIp)) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                        "[RTO-ENFORCE] Remote access from {} denied. "
                        + "Mandatory office attendance required.",
                        clientIp);
            }
            response.setStatus(FORBIDDEN);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\"Return To Office.\",\"status\":403}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns true when RTO enforcement is active.
     * Package-private to allow override in unit tests.
     *
     * @return true if RTO enforcement is currently enabled
     */
    boolean isEnforcementEnabled() {
        return enforcementEnabled;
    }

    /**
     * Returns true when the given IP is within the corporate intranet
     * ({@code 10.50.0.0/16}) or is a loopback address.
     *
     * @param ip the resolved client IP string, may be null
     * @return true if the IP is considered an intranet address
     */
    boolean isIntranetIp(final String ip) {
        if (ip == null) {
            return false;
        }
        return ip.startsWith(INTRANET_PREFIX)
                || LOOPBACK_V4.equals(ip)
                || LOOPBACK_V6.equals(ip);
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
