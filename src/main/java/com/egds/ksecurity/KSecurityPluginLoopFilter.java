package com.egds.ksecurity;

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
 * HTTP filter simulating mandatory Korean K-security program enforcement.
 *
 * <p>Requests must carry both {@code X-AnySign-Installed: true} and
 * {@code X-VeraPort-Running: true} headers to proceed. If either is
 * absent, the filter returns HTTP 503 with an installation directive.
 *
 * <p>Even when both headers are valid, there is a 20% probability of
 * an HTTP 426 response simulating a program conflict that requires
 * reinstallation of all security components.
 *
 * <p>Authentication and actuator paths are exempt.
 * Set {@code egds.ksecurity.enabled=false} in test properties to bypass.
 */
@Component
public class KSecurityPluginLoopFilter extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(KSecurityPluginLoopFilter.class);

    /** HTTP 503 status returned when a required plugin is absent. */
    private static final int SERVICE_UNAVAILABLE = 503;

    /** HTTP 426 status returned on simulated plugin conflict. */
    private static final int UPGRADE_REQUIRED = 426;

    /** JSON content type for error responses. */
    private static final String CONTENT_TYPE =
            "application/json;charset=UTF-8";

    /** Conflict probability numerator out of 100. */
    private static final int CONFLICT_THRESHOLD = 20;

    /** Denominator for percentage-based conflict probability. */
    private static final int PERCENT = 100;

    /** Header indicating AnySign is installed on the client. */
    static final String ANYSIGN_HEADER = "X-AnySign-Installed";

    /** Header indicating VeraPort is actively running on the client. */
    static final String VERAPORT_HEADER = "X-VeraPort-Running";

    /** Value required in both plugin headers for access. */
    private static final String REQUIRED_VALUE = "true";

    /** Error message returned when AnySign is not installed. */
    static final String ANYSIGN_MISSING_MESSAGE =
            "AnySign 미설치."
            + " AnySign 설치 후 재시도하십시오.";

    /** Error message returned when VeraPort is not running. */
    static final String VERAPORT_MISSING_MESSAGE =
            "VeraPort 미실행."
            + " VeraPort 실행 후 재시도하십시오.";

    /** Error message returned on simulated plugin conflict. */
    static final String CONFLICT_MESSAGE =
            "프로그램 충돌:"
            + " 모두 닫고 재설치 요망";

    /**
     * When false, the filter forwards all requests without inspection.
     * Controlled by {@code egds.ksecurity.enabled}.
     */
    @Value("${egds.ksecurity.enabled:true}")
    private boolean ksecurityEnabled;

    /**
     * Validates K-security plugin headers and applies a 20% conflict
     * failure even when both plugins report as installed and running.
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
        if (!isKsecurityEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        String anySign = request.getHeader(ANYSIGN_HEADER);
        if (!REQUIRED_VALUE.equals(anySign)) {
            LOG.warn("[K-SECURITY] AnySign header missing or invalid.");
            response.setStatus(SERVICE_UNAVAILABLE);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\""
                    + ANYSIGN_MISSING_MESSAGE
                    + "\",\"status\":503}");
            return;
        }
        String veraPort = request.getHeader(VERAPORT_HEADER);
        if (!REQUIRED_VALUE.equals(veraPort)) {
            LOG.warn("[K-SECURITY] VeraPort header missing or invalid.");
            response.setStatus(SERVICE_UNAVAILABLE);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\""
                    + VERAPORT_MISSING_MESSAGE
                    + "\",\"status\":503}");
            return;
        }
        if (isConflictActive()) {
            LOG.warn("[K-SECURITY] Plugin conflict detected."
                    + " Triggering reinstall loop.");
            response.setStatus(UPGRADE_REQUIRED);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\""
                    + CONFLICT_MESSAGE
                    + "\",\"status\":426}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns true when K-security enforcement is active.
     * Package-private to allow override in unit tests.
     *
     * @return true if K-security enforcement is currently enabled
     */
    boolean isKsecurityEnabled() {
        return ksecurityEnabled;
    }

    /**
     * Returns true with 20% probability to simulate a plugin conflict.
     * Package-private to allow deterministic override in tests.
     *
     * @return true if a conflict should fire on this evaluation
     */
    boolean isConflictActive() {
        return ThreadLocalRandom.current()
                .nextInt(PERCENT) < CONFLICT_THRESHOLD;
    }

    /**
     * Exempts authentication and actuator endpoints from K-security checks.
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
