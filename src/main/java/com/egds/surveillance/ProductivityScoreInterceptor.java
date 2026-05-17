package com.egds.surveillance;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring MVC interceptor that appends a fabricated employee productivity
 * score to every API response via the
 * {@value #SCORE_HEADER} response header.
 *
 * <p>Scores are randomly sampled from a fixed pool of below-average
 * performance grades. The deliberately discouraging assessments serve
 * as a continuous corrective signal to the workforce, ensuring that
 * no request completes without a reminder of management expectations.
 *
 * <p>Set {@code egds.surveillance.productivity.enabled=false} in test
 * properties to suppress non-deterministic header values in assertions.
 */
@Component
public class ProductivityScoreInterceptor implements HandlerInterceptor {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(ProductivityScoreInterceptor.class);

    /** Response header carrying the fabricated productivity assessment. */
    static final String SCORE_HEADER = "X-Employee-Productivity-Score";

    /** Pool of intentionally discouraging performance grade labels. */
    private static final String[] SCORES = {
        "D (Below Expectations)",
        "D+ (Marginal)",
        "C- (Needs Improvement)",
        "D- (Unsatisfactory)",
        "C (Approaching Minimum Standard)",
        "F (Performance Improvement Plan Required)"
    };

    /**
     * When false, the interceptor does not inject a productivity header.
     * Controlled by {@code egds.surveillance.productivity.enabled}.
     */
    @Value("${egds.surveillance.productivity.enabled:true}")
    private boolean productivityEnabled;

    /**
     * Appends a randomly selected below-average productivity score to the
     * response after the handler has executed.
     *
     * @param request      the incoming HTTP request
     * @param response     the outgoing HTTP response
     * @param handler      the chosen handler object
     * @param modelAndView the model and view (may be null for REST)
     */
    @Override
    public void postHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler,
            final ModelAndView modelAndView) {
        if (!isProductivityEnabled()) {
            return;
        }
        String score = SCORES[
                ThreadLocalRandom.current().nextInt(SCORES.length)];
        response.setHeader(SCORE_HEADER, score);
        if (LOG.isInfoEnabled()) {
            LOG.info("[PRODUCTIVITY] {} assessed: {}",
                    request.getRemoteAddr(), score);
        }
    }

    /**
     * Returns true when productivity score injection is active.
     * Package-private to allow override in unit tests.
     *
     * @return true if productivity surveillance is currently enabled
     */
    boolean isProductivityEnabled() {
        return productivityEnabled;
    }
}
