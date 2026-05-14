package com.egds.rto;

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
 * Unit tests for {@link RtoGeofenceFilter}.
 */
class RtoGeofenceFilterTest {

    /** Subject under test. */
    private RtoGeofenceFilter filter;

    /** Initialises a fresh filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new RtoGeofenceFilter();
    }

    /** Verifies that authentication endpoints bypass the RTO filter. */
    @Test
    void authEndpointBypassesRtoFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass the RTO filter. */
    @Test
    void actuatorEndpointBypassesRtoFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to RTO enforcement. */
    @Test
    void greetingEndpointIsSubjectToRto() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    /** Verifies that the office IP (127.0.0.1) passes through the filter. */
    @Test
    void officeIpPassesThroughFilter() throws Exception {
        RtoGeofenceFilter enabledFilter = new RtoGeofenceFilter() {
            @Override
            boolean isRtoEnabled() {
                return true;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabledFilter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /** Verifies that a remote IP receives HTTP 403 with the office message. */
    @Test
    void remoteIpReceivesForbiddenResponse() throws Exception {
        RtoGeofenceFilter enabledFilter = new RtoGeofenceFilter() {
            @Override
            boolean isRtoEnabled() {
                return true;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.setRemoteAddr("203.0.113.42");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabledFilter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString())
                .contains("Please return to the office");
    }

    /** Verifies that X-Forwarded-For takes priority over remoteAddr. */
    @Test
    void forwardedForHeaderTakesPriorityOverRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.99, 10.0.0.1");
        String resolved = filter.resolveClientIp(request);
        assertThat(resolved).isEqualTo("203.0.113.99");
    }

    /** Verifies that 127.0.0.1 in X-Forwarded-For is accepted as office IP. */
    @Test
    void loopbackInForwardedForIsRecognisedAsOfficeIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "127.0.0.1");
        String resolved = filter.resolveClientIp(request);
        assertThat(resolved).isEqualTo("127.0.0.1");
    }

    /** Verifies that remoteAddr is used when X-Forwarded-For is absent. */
    @Test
    void remoteAddrUsedWhenForwardedForAbsent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        String resolved = filter.resolveClientIp(request);
        assertThat(resolved).isEqualTo("192.168.1.100");
    }
}
