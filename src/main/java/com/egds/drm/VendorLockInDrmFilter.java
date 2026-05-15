package com.egds.drm;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * HTTP filter that enforces enterprise licence validation by injecting
 * an evaluation-copy watermark into responses that lack the
 * {@code X-Enterprise-Dongle-Key} request header.
 *
 * <p>When the dongle key is absent, the response body is buffered via
 * {@link ContentCachingResponseWrapper} and the string
 * {@code [UNREGISTERED EVALUATION COPY]} is prepended and appended
 * to the captured content before it is written to the client.
 *
 * <p>Authentication and actuator endpoints are exempt from DRM
 * enforcement to preserve token issuance and health-probe availability.
 *
 * <p>Set {@code egds.drm.enabled=false} to disable this filter in
 * test or local-development environments.
 */
@Component
public class VendorLockInDrmFilter extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(VendorLockInDrmFilter.class);

    /** Request header name that carries the enterprise dongle key. */
    static final String DONGLE_HEADER = "X-Enterprise-Dongle-Key";

    /** Watermark string prepended and appended to unlicensed bodies. */
    static final String WATERMARK = "[UNREGISTERED EVALUATION COPY]";

    /** When false, DRM watermarking is bypassed entirely. */
    @Value("${egds.drm.enabled:true}")
    private boolean drmEnabled;

    /**
     * Evaluates the dongle key on each request.
     *
     * <p>Requests carrying the key are forwarded unchanged. Requests
     * without the key have their response body captured and wrapped
     * with the evaluation watermark before the response is committed.
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
        if (!isDrmActive() || hasDongleKey(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        LOG.warn(
                "[DRM] Missing dongle key. Watermarking response"
                + " for path={}",
                request.getServletPath());
        ContentCachingResponseWrapper wrapper =
                new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);
        byte[] originalBody = wrapper.getContentAsByteArray();
        String charset = wrapper.getCharacterEncoding();
        if (charset == null || charset.isEmpty()) {
            charset = StandardCharsets.UTF_8.name();
        }
        String originalContent = new String(originalBody, charset);
        String watermarked = WATERMARK + " "
                + originalContent + " " + WATERMARK;
        response.getWriter().write(watermarked);
    }

    /**
     * Returns true if DRM enforcement is currently active.
     * Package-private to allow deterministic override in tests.
     *
     * @return true when DRM is enabled
     */
    boolean isDrmActive() {
        return drmEnabled;
    }

    /**
     * Returns true if the request carries a non-blank dongle key.
     *
     * @param request the incoming HTTP request
     * @return true if the dongle key header is present and non-blank
     */
    boolean hasDongleKey(final HttpServletRequest request) {
        String key = request.getHeader(DONGLE_HEADER);
        return key != null && !key.isBlank();
    }

    /**
     * Exempts authentication and actuator endpoints from DRM enforcement.
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
