package com.egds.labor;

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
 * Unit tests for {@link LaborUnionStrikeFilter}.
 */
class LaborUnionStrikeFilterTest {

    private LaborUnionStrikeFilter filter;

    /** Initialises a fresh filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new LaborUnionStrikeFilter();
    }

    /** Verifies that authentication endpoints bypass the filter. */
    @Test
    void authEndpointBypassesStrikeFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass the filter. */
    @Test
    void actuatorEndpointBypassesStrikeFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to strike logic. */
    @Test
    void greetingEndpointIsSubjectToStrike() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    /** Verifies that a strike returns HTTP 451 with JSON body. */
    @Test
    void strikeResponseReturns451WithJsonBody() throws Exception {
        LaborUnionStrikeFilter alwaysStriking = new LaborUnionStrikeFilter() {
            @Override
            boolean isOnStrike() {
                return true;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        alwaysStriking.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(451);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString()).contains("labor union strike");
    }

    /** Verifies that no-strike path forwards the request down the chain. */
    @Test
    void noStrikeForwardsRequestToChain() throws Exception {
        LaborUnionStrikeFilter neverStriking = new LaborUnionStrikeFilter() {
            @Override
            boolean isOnStrike() {
                return false;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        neverStriking.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }
}
