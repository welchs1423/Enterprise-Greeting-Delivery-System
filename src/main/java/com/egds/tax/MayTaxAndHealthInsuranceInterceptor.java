package com.egds.tax;

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
 * HTTP filter that simulates the May comprehensive income tax settlement
 * and regional health insurance subscriber reclassification.
 *
 * <p>On activation, the response body is modified in two ways:
 * <ol>
 *   <li>The second half of the response content is replaced with
 *       {@code [국세청 압류됨]}, representing an NTS seizure.</li>
 *   <li>A health insurance and tax warning notice is appended to the
 *       truncated body.</li>
 * </ol>
 *
 * <p>Authentication and actuator paths are exempt.
 * Set {@code egds.tax.enabled=false} in test properties to bypass.
 */
@Component
public class MayTaxAndHealthInsuranceInterceptor extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(
                    MayTaxAndHealthInsuranceInterceptor.class);

    /** Marker appended after seizing the second half of the body. */
    static final String SEIZURE_MARKER = "[국세청 압류됨]";

    /** Warning notice appended to every response. */
    static final String TAX_WARNING =
            "[건강보험료 지역가입자 전환"
            + " 및 5월 종소세 추가 징수분]";

    /**
     * When false, the filter passes through without body modification.
     * Controlled by {@code egds.tax.enabled}.
     */
    @Value("${egds.tax.enabled:true}")
    private boolean taxEnabled;

    /**
     * Wraps the response, caches the body written by downstream handlers,
     * then seizes the second half and appends the tax warning.
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
        if (!isTaxEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        LOG.warn("[TAX] 5월 종소세 정산 및"
                + " 건보료 지역가입자 전환 처리 중.");
        ContentCachingResponseWrapper wrapper =
                new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        byte[] body = wrapper.getContentAsByteArray();
        String content = new String(body, StandardCharsets.UTF_8);
        String settled = applyTaxSettlement(content);
        byte[] resultBytes = settled.getBytes(StandardCharsets.UTF_8);

        response.setContentLength(resultBytes.length);
        response.getOutputStream().write(resultBytes);
    }

    /**
     * Returns true when tax enforcement is active.
     * Package-private to allow override in unit tests.
     *
     * @return true if tax enforcement is currently enabled
     */
    boolean isTaxEnabled() {
        return taxEnabled;
    }

    /**
     * Replaces the second half of {@code content} with the NTS seizure
     * marker and appends the health insurance and tax warning notice.
     *
     * @param content the original response body string
     * @return the seized and annotated string
     */
    String applyTaxSettlement(final String content) {
        int mid = content.length() / 2;
        String firstHalf = content.substring(0, mid);
        return firstHalf + SEIZURE_MARKER + TAX_WARNING;
    }

    /**
     * Exempts authentication and actuator endpoints from tax settlement.
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
