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
 * Unit tests for {@link RtoEnforcementFilter}.
 */
class RtoEnforcementFilterTest {

    /** Subject under test. */
    private RtoEnforcementFilter filter;

    /** Initialises a fresh filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new RtoEnforcementFilter();
    }

    /** Verifies that authentication endpoints bypass RTO enforcement. */
    @Test
    void authEndpointBypassesEnforcement() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass RTO enforcement. */
    @Test
    void actuatorEndpointBypassesEnforcement() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to enforcement. */
    @Test
    void greetingEndpointIsSubjectToEnforcement() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    /** Verifies that a 10.50.x.x intranet IP passes through. */
    @Test
    void intranetIpPassesThroughFilter() throws Exception {
        RtoEnforcementFilter enabled = new RtoEnforcementFilter() {
            @Override
            boolean isEnforcementEnabled() {
                return true;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.setRemoteAddr("10.50.12.99");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabled.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /** Verifies that a remote IP receives HTTP 403 with the RTO message. */
    @Test
    void remoteIpReceivesForbiddenResponse() throws Exception {
        RtoEnforcementFilter enabled = new RtoEnforcementFilter() {
            @Override
            boolean isEnforcementEnabled() {
                return true;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.setRemoteAddr("203.0.113.5");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabled.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString())
                .contains("Return To Office.");
    }

    /** Verifies that X-Bypass-Rto header grants passage regardless of IP. */
    @Test
    void bypassHeaderGrantsAccess() throws Exception {
        RtoEnforcementFilter enabled = new RtoEnforcementFilter() {
            @Override
            boolean isEnforcementEnabled() {
                return true;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.setRemoteAddr("203.0.113.5");
        request.addHeader("X-Bypass-Rto", "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabled.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /** Verifies that loopback addresses are accepted as intranet. */
    @Test
    void loopbackAddressesAreAcceptedAsIntranet() {
        assertThat(filter.isIntranetIp("127.0.0.1")).isTrue();
        assertThat(filter.isIntranetIp("::1")).isTrue();
    }

    /** Verifies that 10.50.x.x addresses are accepted as intranet. */
    @Test
    void intranetSubnetIsAccepted() {
        assertThat(filter.isIntranetIp("10.50.0.1")).isTrue();
        assertThat(filter.isIntranetIp("10.50.255.254")).isTrue();
    }

    /** Verifies that non-intranet addresses are rejected. */
    @Test
    void nonIntranetAddressesAreRejected() {
        assertThat(filter.isIntranetIp("192.168.1.1")).isFalse();
        assertThat(filter.isIntranetIp("10.0.1.1")).isFalse();
        assertThat(filter.isIntranetIp("10.51.0.1")).isFalse();
    }

    /** Verifies that null IP is treated as non-intranet. */
    @Test
    void nullIpIsRejected() {
        assertThat(filter.isIntranetIp(null)).isFalse();
    }

    /** Verifies that X-Forwarded-For takes priority over remoteAddr. */
    @Test
    void forwardedForHeaderTakesPriority() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.50.1.1");
        request.addHeader("X-Forwarded-For", "203.0.113.9, 10.50.1.1");
        assertThat(filter.resolveClientIp(request)).isEqualTo("203.0.113.9");
    }

    /** Verifies that remoteAddr is used when X-Forwarded-For is absent. */
    @Test
    void remoteAddrUsedWhenForwardedForAbsent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.50.3.77");
        assertThat(filter.resolveClientIp(request)).isEqualTo("10.50.3.77");
    }
}
