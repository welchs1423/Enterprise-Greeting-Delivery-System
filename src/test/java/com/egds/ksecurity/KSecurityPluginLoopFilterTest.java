package com.egds.ksecurity;

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
 * Unit tests for {@link KSecurityPluginLoopFilter}.
 */
class KSecurityPluginLoopFilterTest {

    /** Filter under test. */
    private KSecurityPluginLoopFilter filter;

    /** Initialises a fresh filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new KSecurityPluginLoopFilter();
    }

    /** Verifies that auth endpoints bypass K-security enforcement. */
    @Test
    void authEndpointBypassesFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass K-security enforcement. */
    @Test
    void actuatorEndpointBypassesFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to K-security. */
    @Test
    void greetingEndpointIsSubjectToKsecurity() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    /** Verifies that missing AnySign header returns HTTP 503. */
    @Test
    void missingAnySignHeaderReturns503() throws Exception {
        KSecurityPluginLoopFilter enabled = enabledNoConflictFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.addHeader(
                KSecurityPluginLoopFilter.VERAPORT_HEADER, "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabled.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getContentAsString())
                .contains(
                        KSecurityPluginLoopFilter.ANYSIGN_MISSING_MESSAGE);
    }

    /** Verifies that missing VeraPort header returns HTTP 503. */
    @Test
    void missingVeraPortHeaderReturns503() throws Exception {
        KSecurityPluginLoopFilter enabled = enabledNoConflictFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.addHeader(
                KSecurityPluginLoopFilter.ANYSIGN_HEADER, "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabled.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getContentAsString())
                .contains(
                        KSecurityPluginLoopFilter.VERAPORT_MISSING_MESSAGE);
    }

    /**
     * Verifies that a simulated conflict returns HTTP 426 even when both
     * plugin headers are present.
     */
    @Test
    void conflictActiveReturns426WithBothHeaders() throws Exception {
        KSecurityPluginLoopFilter alwaysConflict =
                new KSecurityPluginLoopFilter() {
                    @Override
                    boolean isKsecurityEnabled() {
                        return true;
                    }

                    @Override
                    boolean isConflictActive() {
                        return true;
                    }
                };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.addHeader(
                KSecurityPluginLoopFilter.ANYSIGN_HEADER, "true");
        request.addHeader(
                KSecurityPluginLoopFilter.VERAPORT_HEADER, "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        alwaysConflict.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(426);
        assertThat(response.getContentAsString())
                .contains(KSecurityPluginLoopFilter.CONFLICT_MESSAGE);
    }

    /**
     * Verifies that both valid headers with no conflict forwards the
     * request to the chain.
     */
    @Test
    void bothHeadersPresentNoConflictForwardsRequest() throws Exception {
        KSecurityPluginLoopFilter enabled = enabledNoConflictFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.addHeader(
                KSecurityPluginLoopFilter.ANYSIGN_HEADER, "true");
        request.addHeader(
                KSecurityPluginLoopFilter.VERAPORT_HEADER, "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        enabled.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /** Verifies that when disabled, all requests pass through. */
    @Test
    void disabledFilterPassesAllRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    private KSecurityPluginLoopFilter enabledNoConflictFilter() {
        return new KSecurityPluginLoopFilter() {
            @Override
            boolean isKsecurityEnabled() {
                return true;
            }

            @Override
            boolean isConflictActive() {
                return false;
            }
        };
    }
}
