package com.egds.surveillance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit tests for {@link MouseJigglerDetector}.
 */
class MouseJigglerDetectorTest {

    /** Subject under test. */
    private MouseJigglerDetector detector;

    /** Initialises a fresh detector before each test. */
    @BeforeEach
    void setUp() {
        detector = new MouseJigglerDetector();
    }

    /** Verifies that authentication endpoints bypass jiggler detection. */
    @Test
    void authEndpointBypassesDetector() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(detector.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass jiggler detection. */
    @Test
    void actuatorEndpointBypassesDetector() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(detector.shouldNotFilter(request)).isTrue();
    }

    /** Verifies detection fires after three consistent 1000ms intervals. */
    @Test
    void macroPatternDetectedAfterThreeConsistentIntervals() {
        long base = 100_000L;
        detector.isMacroPattern("10.0.0.1", base);
        detector.isMacroPattern("10.0.0.1", base + 1000);
        detector.isMacroPattern("10.0.0.1", base + 2000);
        boolean detected =
                detector.isMacroPattern("10.0.0.1", base + 3000);
        assertThat(detected).isTrue();
    }

    /** Verifies detection fires when intervals are within jitter tolerance. */
    @Test
    void macroPatternDetectedWithinJitterTolerance() {
        long base = 200_000L;
        detector.isMacroPattern("10.0.0.4", base);
        detector.isMacroPattern("10.0.0.4", base + 1000);
        detector.isMacroPattern("10.0.0.4", base + 2007);
        boolean detected =
                detector.isMacroPattern("10.0.0.4", base + 3005);
        assertThat(detected).isTrue();
    }

    /** Verifies that irregular intervals are not flagged as macro. */
    @Test
    void irregularIntervalsAreNotFlagged() {
        long base = 100_000L;
        detector.isMacroPattern("10.0.0.2", base);
        detector.isMacroPattern("10.0.0.2", base + 500);
        detector.isMacroPattern("10.0.0.2", base + 2000);
        boolean detected =
                detector.isMacroPattern("10.0.0.2", base + 5000);
        assertThat(detected).isFalse();
    }

    /** Verifies that fewer than 4 timestamps do not trigger a block. */
    @Test
    void insufficientDataDoesNotTriggerBlock() {
        long base = 100_000L;
        detector.isMacroPattern("10.0.0.3", base);
        detector.isMacroPattern("10.0.0.3", base + 1000);
        boolean detected =
                detector.isMacroPattern("10.0.0.3", base + 2000);
        assertThat(detected).isFalse();
    }

    /** Verifies that detected macro pattern returns HTTP 429 with HR message. */
    @Test
    void macroPatternReturns429WithHrMessage() throws Exception {
        MouseJigglerDetector alwaysMacro = new MouseJigglerDetector() {
            @Override
            boolean isMacroPattern(final String ip, final long nowMs) {
                return true;
            }

            @Override
            boolean isJigglerEnabled() {
                return true;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        alwaysMacro.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentType())
                .startsWith("application/json");
        assertThat(response.getContentAsString())
                .contains("HR 팀에 보고되었습니다");
    }

    /** Verifies that a disabled detector forwards all requests. */
    @Test
    void disabledDetectorForwardsAllRequests() throws Exception {
        MouseJigglerDetector disabled = new MouseJigglerDetector() {
            @Override
            boolean isJigglerEnabled() {
                return false;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        disabled.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /** Verifies that different IPs maintain independent timestamp windows. */
    @Test
    void differentIpsMaintainIndependentWindows() {
        long base = 100_000L;
        // IP A: consistent pattern
        detector.isMacroPattern("10.0.0.5", base);
        detector.isMacroPattern("10.0.0.5", base + 1000);
        detector.isMacroPattern("10.0.0.5", base + 2000);
        detector.isMacroPattern("10.0.0.5", base + 3000);

        // IP B: only two timestamps, should not trigger
        detector.isMacroPattern("10.0.0.6", base);
        boolean detectedB =
                detector.isMacroPattern("10.0.0.6", base + 1000);
        assertThat(detectedB).isFalse();
    }

    /** Verifies that X-Forwarded-For is used for IP resolution. */
    @Test
    void forwardedForHeaderUsedForIpResolution() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.1");
        assertThat(detector.resolveClientIp(request))
                .isEqualTo("203.0.113.7");
    }
}
