package com.egds.monetization;

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
 * V21 강제 광고 시청 필터.
 *
 * <p>프리미엄 구독자가 아닌 모든 사용자에게 5초간 강제 광고를
 * 시청시킵니다. {@code X-Premium-Subscription: true} 헤더를
 * 보유한 요청은 즉시 다음 필터로 전달됩니다.
 *
 * <p>인증({@code /api/v1/auth/**}) 및 액추에이터 경로는
 * 광고 딜레이에서 제외됩니다.
 *
 * <p>{@code egds.ads.enabled=false}로 설정하면 광고 딜레이가
 * 비활성화됩니다 (테스트 환경 플레이크 방지용).
 */
@Component
public class UnskippableAdFilter extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(UnskippableAdFilter.class);

    /** Header name indicating premium subscription status. */
    private static final String PREMIUM_HEADER =
            "X-Premium-Subscription";

    /** Expected header value for premium subscribers. */
    private static final String PREMIUM_VALUE = "true";

    /** Forced ad-viewing duration in milliseconds. */
    private static final long AD_DURATION_MS = 5000L;

    /** When false, the ad delay never fires (e.g., in tests). */
    @Value("${egds.ads.enabled:true}")
    private boolean adsEnabled;

    /**
     * Enforces a mandatory ad-viewing delay for non-premium requests.
     * Premium subscribers and disabled-flag mode bypass the delay.
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
        if (adsEnabled && !isPremium(request)) {
            LOG.info("[광고 시청 중...] 기업용 슬러시를 구매하세요!");
            performAdDelay();
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns true if the request carries a valid premium header.
     *
     * @param request the incoming HTTP request
     * @return true if the subscriber holds a premium subscription
     */
    boolean isPremium(final HttpServletRequest request) {
        return PREMIUM_VALUE.equals(
                request.getHeader(PREMIUM_HEADER));
    }

    /**
     * Executes the ad-viewing delay. Overridable in tests to skip
     * the actual {@code Thread.sleep} without altering filter logic.
     */
    void performAdDelay() {
        try {
            Thread.sleep(AD_DURATION_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Exempts authentication and actuator endpoints from ad delay.
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
