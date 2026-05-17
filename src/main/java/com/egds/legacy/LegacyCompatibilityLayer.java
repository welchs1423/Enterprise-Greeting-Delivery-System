package com.egds.legacy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * HTTP filter that converts modern JSON responses to a legacy Oracle
 * ROWSET XML dump format with 10% probability, simulating mandatory
 * Xplatform dataset compatibility enforcement from the Oracle 9i era.
 *
 * <p>When activated, the original response body is wrapped in a legacy
 * XML envelope and the Content-Type is rewritten to {@code text/xml}.
 * Authentication and actuator endpoints are exempt.
 * Set {@code egds.legacy.compatibility.enabled=false} to disable.
 */
@Component
public class LegacyCompatibilityLayer extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(LegacyCompatibilityLayer.class);

    /** Legacy conversion activation probability numerator out of 100. */
    private static final int LEGACY_THRESHOLD = 10;

    /** Denominator for percentage-based activation probability. */
    private static final int PERCENT = 100;

    /** XML Content-Type written on legacy conversion. */
    static final String LEGACY_CONTENT_TYPE = "text/xml;charset=UTF-8";

    /** When false, legacy conversion is bypassed entirely. */
    @Value("${egds.legacy.compatibility.enabled:true}")
    private boolean legacyEnabled;

    /**
     * Buffers the downstream response and converts it to legacy Oracle
     * XML format with {@value #LEGACY_THRESHOLD}% probability.
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
        if (!isLegacyConversionActive()) {
            filterChain.doFilter(request, response);
            return;
        }
        ContentCachingResponseWrapper wrapper =
                new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);
        byte[] body = wrapper.getContentAsByteArray();
        String charset = wrapper.getCharacterEncoding();
        if (charset == null || charset.isEmpty()) {
            charset = StandardCharsets.UTF_8.name();
        }
        String content = new String(body, charset);
        String legacyXml = toLegacyOracleXml(content);
        byte[] legacyBytes =
                legacyXml.getBytes(StandardCharsets.UTF_8);
        LOG.warn("[WARN] Legacy Oracle DB sync delayed");
        response.setContentType(LEGACY_CONTENT_TYPE);
        response.setContentLength(legacyBytes.length);
        response.getOutputStream().write(legacyBytes);
    }

    /**
     * Returns true with 10% probability when legacy conversion is enabled.
     * Package-private to allow deterministic override in tests.
     *
     * @return true if the conversion fires on this evaluation
     */
    boolean isLegacyConversionActive() {
        return legacyEnabled
                && ThreadLocalRandom.current()
                        .nextInt(PERCENT) < LEGACY_THRESHOLD;
    }

    /**
     * Wraps the supplied payload in a legacy Oracle ROWSET XML envelope,
     * emulating Xplatform dataset output from an Oracle 9i integration.
     * Ampersands, less-than, and greater-than characters in the original
     * content are entity-escaped before embedding.
     *
     * @param json the original response body
     * @return XML string in legacy Oracle dump format
     */
    String toLegacyOracleXml(final String json) {
        String escaped = json.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!-- XPLATFORM DATASET v2.0 -->\n"
                + "<ROWSET>\n"
                + "  <ROW NUM=\"1\">\n"
                + "    <LEGACY_DATA>"
                + escaped
                + "</LEGACY_DATA>\n"
                + "    <SYNC_STATUS>DELAYED</SYNC_STATUS>\n"
                + "    <ORACLE_VERSION>"
                + "Oracle Database 9i"
                + "</ORACLE_VERSION>\n"
                + "  </ROW>\n"
                + "</ROWSET>";
    }

    /**
     * Exempts authentication and actuator endpoints from legacy conversion.
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
