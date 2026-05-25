package com.egds.nac;

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
 * HTTP filter enforcing MAC address registration via corporate NAC policy.
 *
 * <p>Every request must carry both the {@code X-MAC-Address} header and
 * the {@code X-Chajangnim-Port-Opened: true} header, indicating that the
 * deputy manager has approved and opened the switch port for the device.
 * Requests lacking the manager approval header are rejected with
 * HTTP 401 and a Korean-language remediation notice.
 *
 * <p>Authentication and actuator paths are exempt.
 * Set {@code egds.nac.enabled=false} in test properties to bypass.
 */
@Component
public class MacAddressNacFilter extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(MacAddressNacFilter.class);

    /** HTTP 401 status returned when MAC / port approval is absent. */
    private static final int UNAUTHORIZED = 401;

    /** JSON content type for rejection responses. */
    private static final String CONTENT_TYPE =
            "application/json;charset=UTF-8";

    /** Header carrying the device MAC address. */
    static final String MAC_HEADER = "X-MAC-Address";

    /** Header set to {@code true} when the deputy manager opened the port. */
    static final String APPROVAL_HEADER = "X-Chajangnim-Port-Opened";

    /** Value required in {@link #APPROVAL_HEADER} for access. */
    private static final String APPROVAL_VALUE = "true";

    /** Korean error message returned on missing approval. */
    static final String UNREGISTERED_MESSAGE =
            "MAC 주소 미등록."
            + " 차장님이나 전산팀에"
            + " 맥주소 전달 후"
            + " 포트 개방을"
            + " 요청하십시오.";

    /**
     * When false, the filter forwards all requests without inspection.
     * Controlled by {@code egds.nac.enabled}.
     */
    @Value("${egds.nac.enabled:true}")
    private boolean nacEnabled;

    /**
     * Checks for manager port-opening approval on each request. Rejects
     * with HTTP 401 if the approval header is absent or not {@code true}.
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
        if (!isNacEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        String approvalHeader = request.getHeader(APPROVAL_HEADER);
        if (!APPROVAL_VALUE.equals(approvalHeader)) {
            String macAddress = request.getHeader(MAC_HEADER);
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                        "[NAC] Port not opened by manager."
                        + " MAC={} Approval={}",
                        macAddress,
                        approvalHeader);
            }
            response.setStatus(UNAUTHORIZED);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\""
                    + UNREGISTERED_MESSAGE
                    + "\",\"status\":401}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns true when the NAC filter is disabled.
     * Package-private to allow override in unit tests.
     *
     * @return true if the filter is currently disabled
     */
    boolean isNacEnabled() {
        return nacEnabled;
    }

    /**
     * Exempts authentication and actuator endpoints from NAC enforcement.
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
