package com.egds.esg;

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
 * Unit tests for {@link EsgGreenwashingInterceptor}.
 */
class EsgGreenwashingInterceptorTest {

    private EsgGreenwashingInterceptor filter;

    /** Initialises a disabled-greenwashing filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new EsgGreenwashingInterceptor() {
            @Override
            void applySolarDelay() {
                // no-op: skip 2-second sleep in unit tests
            }
        };
    }

    /** Verifies that auth endpoints bypass the filter. */
    @Test
    void authEndpointBypassesInterceptor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass the filter. */
    @Test
    void actuatorEndpointBypassesInterceptor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to the filter. */
    @Test
    void greetingEndpointIsSubjectToInterceptor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    /**
     * Verifies that when greenwashing is disabled the request passes
     * through the filter chain without body modification.
     */
    @Test
    void disabledFilterPassesThroughChain() throws Exception {
        EsgGreenwashingInterceptor disabled =
                new EsgGreenwashingInterceptor();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        disabled.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    /** Verifies that "Hello" in a response body receives the eco prefix. */
    @Test
    void injectEcoPrefixReplacesHelloMarker() {
        String result = filter.injectEcoPrefix(
                "{\"content\":\"Hello, World!\"}");
        assertThat(result).contains("🌱 [Eco-Friendly] Hello");
        assertThat(result).doesNotContain("\"Hello,");
    }

    /** Verifies that content without "Hello" is returned unchanged. */
    @Test
    void injectEcoPrefixLeavesNonHelloContentUnchanged() {
        String input = "{\"correlationId\":\"abc-123\"}";
        assertThat(filter.injectEcoPrefix(input)).isEqualTo(input);
    }
}
