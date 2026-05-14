package com.egds.capitalism;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * HTTP filter that truncates response bodies based on the per-request
 * character budget declared in the {@code X-Client-Budget} header.
 *
 * <p>Clients that omit the header receive the full response. Clients
 * that supply a positive budget smaller than the response length
 * receive the first {@code budget} characters followed by
 * {@code "..."}. Clients with a non-positive budget receive HTTP 402.
 */
@Component
public class MicroTransactionTruncator extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(MicroTransactionTruncator.class);

    /** HTTP header carrying the per-request character budget. */
    private static final String BUDGET_HEADER = "X-Client-Budget";

    /** Ellipsis appended when response content is truncated. */
    private static final String ELLIPSIS = "...";

    /** HTTP status returned when the declared budget is non-positive. */
    private static final int PAYMENT_REQUIRED = 402;

    /** Content type for billing error responses. */
    private static final String JSON_CONTENT_TYPE = "application/json";

    /**
     * Applies budget-based truncation to the response body.
     * Requests without the {@code X-Client-Budget} header bypass
     * truncation entirely.
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
        String budgetHeader = request.getHeader(BUDGET_HEADER);
        if (budgetHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        int budget;
        try {
            budget = Integer.parseInt(budgetHeader.trim());
        } catch (NumberFormatException e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (budget <= 0) {
            response.setStatus(PAYMENT_REQUIRED);
            response.setContentType(JSON_CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\"Insufficient budget.\","
                    + "\"status\":402}");
            return;
        }

        ContentCachingResponseWrapper wrapper =
                new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        byte[] body = wrapper.getContentAsByteArray();
        String content = new String(body, StandardCharsets.UTF_8);
        String result = applyBudget(content, budget);
        byte[] resultBytes = result.getBytes(StandardCharsets.UTF_8);

        response.setContentLength(resultBytes.length);
        response.getOutputStream().write(resultBytes);

        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "[BILLING] budget={} original={} delivered={}",
                    budget, content.length(), result.length());
        }
    }

    /**
     * Truncates {@code content} to at most {@code budget} characters,
     * appending {@code "..."} when truncation occurs.
     *
     * @param content the full response content string
     * @param budget  the maximum number of characters to deliver
     * @return the original string if it fits, otherwise the first
     *         {@code budget} characters followed by {@code "..."}
     */
    String applyBudget(final String content, final int budget) {
        if (content.length() <= budget) {
            return content;
        }
        return content.substring(0, budget) + ELLIPSIS;
    }
}
