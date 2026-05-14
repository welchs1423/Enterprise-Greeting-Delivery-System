package com.egds.esg;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * HTTP filter that simulates ESG compliance by delaying greeting
 * responses and prepending an eco-friendly label to message content.
 *
 * <p>On activation, blocks the request thread for two seconds while
 * emitting a solar-power readiness log entry, then replaces every
 * occurrence of "Hello" in the response body with the eco-labelled
 * variant.
 *
 * <p>Authentication and actuator paths bypass this filter.
 * Set {@code egds.esg.greenwashing.enabled=false} in test properties
 * to prevent latency in automated tests.
 */
@Component
public class EsgGreenwashingInterceptor extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(EsgGreenwashingInterceptor.class);

    /** Seconds the thread is blocked to simulate solar-energy sourcing. */
    private static final long SOLAR_DELAY_SECONDS = 2L;

    /** Substring matched for eco-friendly prefix injection. */
    private static final String HELLO_MARKER = "Hello";

    /**
     * Eco-friendly replacement prefix substituted for each "Hello"
     * occurrence in the response body. Unicode surrogate pair U+1F331.
     */
    private static final String ECO_PREFIX =
            "🌱 [Eco-Friendly] Hello";

    /**
     * When false, the filter passes through without delay or body
     * modification. Controlled by {@code egds.esg.greenwashing.enabled}.
     */
    @Value("${egds.esg.greenwashing.enabled:true}")
    private boolean greenwashingEnabled;

    /**
     * Applies the ESG greenwashing delay and response body modification
     * to all non-exempt requests. When the feature is disabled, the
     * request passes through unmodified.
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
        if (!greenwashingEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        applySolarDelay();

        ContentCachingResponseWrapper wrapper =
                new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        byte[] body = wrapper.getContentAsByteArray();
        String content = new String(body, StandardCharsets.UTF_8);
        String result = injectEcoPrefix(content);
        byte[] resultBytes = result.getBytes(StandardCharsets.UTF_8);

        response.setContentLength(resultBytes.length);
        response.getOutputStream().write(resultBytes);
    }

    /**
     * Blocks the calling thread for {@link #SOLAR_DELAY_SECONDS} seconds
     * and emits the greenwashing log entry.
     */
    void applySolarDelay() {
        LOG.info("[ESG] 친환경 태양광 발전 "
                + "대기 중...");
        try {
            TimeUnit.SECONDS.sleep(SOLAR_DELAY_SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Replaces every occurrence of "Hello" in {@code content} with the
     * eco-friendly prefixed variant.
     *
     * @param content the original response body string
     * @return the modified string, or the original if "Hello" is absent
     */
    String injectEcoPrefix(final String content) {
        return content.replace(HELLO_MARKER, ECO_PREFIX);
    }

    /**
     * Exempts authentication and actuator endpoints from greenwashing.
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
