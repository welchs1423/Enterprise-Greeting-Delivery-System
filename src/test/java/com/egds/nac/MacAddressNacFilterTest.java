package com.egds.nac;

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
 * Unit tests for {@link MacAddressNacFilter}.
 */
class MacAddressNacFilterTest {

    /** Filter under test. */
    private MacAddressNacFilter filter;

    /** Initialises a fresh filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new MacAddressNacFilter();
    }

    /** Verifies that auth endpoints bypass NAC enforcement. */
    @Test
    void authEndpointBypassesNacFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass NAC enforcement. */
    @Test
    void actuatorEndpointBypassesNacFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to NAC logic. */
    @Test
    void greetingEndpointIsSubjectToNac() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    /**
     * Verifies that a missing manager approval header returns HTTP 401.
     */
    @Test
    void missingApprovalHeaderReturns401() throws Exception {
        MacAddressNacFilter enabled = enabledFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.addHeader(
                MacAddressNacFilter.MAC_HEADER,
                "AA:BB:CC:DD:EE:FF");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabled.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType())
                .contains("application/json");
        assertThat(response.getContentAsString())
                .contains(MacAddressNacFilter.UNREGISTERED_MESSAGE);
    }

    /**
     * Verifies that wrong approval value (not "true") returns HTTP 401.
     */
    @Test
    void wrongApprovalValueReturns401() throws Exception {
        MacAddressNacFilter enabled = enabledFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.addHeader(
                MacAddressNacFilter.MAC_HEADER,
                "AA:BB:CC:DD:EE:FF");
        request.addHeader(
                MacAddressNacFilter.APPROVAL_HEADER,
                "false");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabled.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    /**
     * Verifies that both headers present allows the request through.
     */
    @Test
    void bothHeadersPresentAllowsRequest() throws Exception {
        MacAddressNacFilter enabled = enabledFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.addHeader(
                MacAddressNacFilter.MAC_HEADER,
                "AA:BB:CC:DD:EE:FF");
        request.addHeader(
                MacAddressNacFilter.APPROVAL_HEADER,
                "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabled.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /**
     * Verifies that when disabled, all requests pass through unmodified.
     */
    @Test
    void disabledFilterPassesAllRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    private MacAddressNacFilter enabledFilter() {
        return new MacAddressNacFilter() {
            @Override
            boolean isNacEnabled() {
                return true;
            }
        };
    }
}
