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
 * Unit tests for {@link UnionStrikeInterceptor}.
 */
class UnionStrikeInterceptorTest {

    /** Filter under test. */
    private UnionStrikeInterceptor interceptor;

    /** Initialises a fresh interceptor before each test. */
    @BeforeEach
    void setUp() {
        interceptor = new UnionStrikeInterceptor();
    }

    /** Verifies that authentication endpoints bypass the interceptor. */
    @Test
    void authEndpointBypassesInterceptor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(interceptor.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass the interceptor. */
    @Test
    void actuatorEndpointBypassesInterceptor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(interceptor.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to strike logic. */
    @Test
    void greetingEndpointIsSubjectToStrike() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(interceptor.shouldNotFilter(request)).isFalse();
    }

    /** Verifies that a strike returns HTTP 503 with JSON body. */
    @Test
    void strikeResponseReturns503WithJsonBody() throws Exception {
        UnionStrikeInterceptor alwaysStriking =
                new UnionStrikeInterceptor() {
                    @Override
                    boolean isStrikeActive() {
                        return true;
                    }
                };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        alwaysStriking.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getContentType())
                .contains("application/json");
        assertThat(response.getContentAsString())
                .contains(UnionStrikeInterceptor.STRIKE_MESSAGE);
    }

    /** Verifies that no-strike path forwards the request down the chain. */
    @Test
    void noStrikeForwardsRequestToChain() throws Exception {
        UnionStrikeInterceptor neverStriking =
                new UnionStrikeInterceptor() {
                    @Override
                    boolean isStrikeActive() {
                        return false;
                    }
                };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        neverStriking.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /** Verifies that isStrikeActive returns false when disabled. */
    @Test
    void isStrikeActiveReturnsFalseWhenDisabled() {
        UnionStrikeInterceptor disabled =
                new UnionStrikeInterceptor() {
                    @Override
                    boolean isStrikeActive() {
                        return false;
                    }
                };
        assertThat(disabled.isStrikeActive()).isFalse();
    }
}
