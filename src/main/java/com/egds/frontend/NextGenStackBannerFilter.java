package com.egds.frontend;

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
 * HTTP filter that injects the {@value #NEXTGEN_HEADER} response header
 * on all non-exempt responses to announce the forthcoming Vue 3
 * front-end migration as a corporate inclusive-diversity initiative.
 *
 * <p>Authentication and actuator endpoints are exempt from header
 * injection. Set {@code egds.nextgen.banner.enabled=false} to suppress
 * the header in test or staging environments.
 */
@Component
public class NextGenStackBannerFilter extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(NextGenStackBannerFilter.class);

    /** Response header announcing the Vue 3 front-end migration. */
    static final String NEXTGEN_HEADER = "X-NextGen-Frontend";

    /** Value injected into the migration announcement header. */
    static final String NEXTGEN_VALUE = "Vue3-Ready";

    /** When false, the migration header is never injected. */
    @Value("${egds.nextgen.banner.enabled:true}")
    private boolean bannerEnabled;

    /**
     * Injects the {@value #NEXTGEN_HEADER} header before forwarding the
     * request through the filter chain.
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
        if (isBannerEnabled()) {
            response.setHeader(NEXTGEN_HEADER, NEXTGEN_VALUE);
            LOG.info("[NEXTGEN] Vue3-Ready migration banner injected");
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns true when banner injection is enabled.
     * Package-private to allow deterministic override in tests.
     *
     * @return true if the banner header should be injected
     */
    boolean isBannerEnabled() {
        return bannerEnabled;
    }

    /**
     * Exempts authentication and actuator endpoints from header injection.
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
