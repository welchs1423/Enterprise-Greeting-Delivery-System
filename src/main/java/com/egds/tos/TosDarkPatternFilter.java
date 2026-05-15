package com.egds.tos;

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
 * V21 약관 강제 동의 다크 패턴 필터.
 *
 * <p>모든 요청은 {@code X-Accept-ToS-Version: 21.0} 헤더를
 * 포함해야 합니다. 헤더가 없거나 버전이 불일치하면 HTTP 451과
 * 함께 영혼 귀속 약관 동의 안내 메시지를 반환합니다.
 *
 * <p>인증({@code /api/v1/auth/**}) 및 액추에이터 경로는
 * 약관 검사에서 제외됩니다.
 *
 * <p>{@code egds.tos.enabled=false}로 설정하면 약관 검사가
 * 비활성화됩니다 (테스트 환경 플레이크 방지용).
 */
@Component
public class TosDarkPatternFilter extends OncePerRequestFilter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(TosDarkPatternFilter.class);

    /** Header name carrying the accepted Terms-of-Service version. */
    private static final String TOS_HEADER = "X-Accept-ToS-Version";

    /** The only acceptable ToS version value. */
    private static final String REQUIRED_VERSION = "21.0";

    /** HTTP status returned when ToS acceptance is missing or invalid. */
    private static final int TOS_BLOCK_STATUS = 451;

    /** Content type with UTF-8 charset for ToS block responses. */
    private static final String CONTENT_TYPE =
            "application/json;charset=UTF-8";

    /** When false, the ToS check never fires (e.g., in tests). */
    @Value("${egds.tos.enabled:true}")
    private boolean tosEnabled;

    /**
     * Enforces ToS version acceptance before processing each request.
     * Returns HTTP 451 with a soul-binding clause message if the
     * required header is absent or carries an incorrect version.
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
        if (tosEnabled && !isTosAccepted(request)) {
            LOG.warn("[ToS] Client rejected. "
                    + "Missing or invalid ToS version header.");
            response.setStatus(TOS_BLOCK_STATUS);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(
                    "{\"error\":\"고객님의 영혼 귀속을 포함한"
                    + " 신규 약관 21.0에 동의해야 합니다.\","
                    + "\"status\":451}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns true if the request carries the required ToS version.
     *
     * @param request the incoming HTTP request
     * @return true if ToS version {@value #REQUIRED_VERSION} is present
     */
    boolean isTosAccepted(final HttpServletRequest request) {
        return REQUIRED_VERSION.equals(
                request.getHeader(TOS_HEADER));
    }

    /**
     * Exempts authentication and actuator endpoints from ToS enforcement.
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
