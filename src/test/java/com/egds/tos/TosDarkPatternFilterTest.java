package com.egds.tos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link TosDarkPatternFilter}.
 */
class TosDarkPatternFilterTest {

    private TosDarkPatternFilter filter;

    /** Initialises a fresh filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new TosDarkPatternFilter();
    }

    /** Verifies that authentication endpoints bypass the ToS filter. */
    @Test
    void authEndpointBypassesTosFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass the ToS filter. */
    @Test
    void actuatorEndpointBypassesTosFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies greeting endpoints are subject to ToS enforcement. */
    @Test
    void greetingEndpointIsSubjectToToS() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    /** Verifies exact ToS version 21.0 is accepted. */
    @Test
    void correctTosVersionIsAccepted() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Accept-ToS-Version", "21.0");
        assertThat(filter.isTosAccepted(request)).isTrue();
    }

    /** Verifies missing ToS header is rejected. */
    @Test
    void missingTosHeaderIsRejected() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThat(filter.isTosAccepted(request)).isFalse();
    }

    /** Verifies wrong ToS version (e.g. 20.0) is rejected. */
    @Test
    void wrongTosVersionIsRejected() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Accept-ToS-Version", "20.0");
        assertThat(filter.isTosAccepted(request)).isFalse();
    }

    /** Verifies missing ToS returns HTTP 451 with JSON body. */
    @Test
    void missingTosReturns451WithJsonBody() throws Exception {
        TosDarkPatternFilter alwaysBlock = new TosDarkPatternFilter() {
            @Override
            boolean isTosAccepted(
                    final jakarta.servlet.http.HttpServletRequest req) {
                return false;
            }
        };
        ReflectionTestUtils.setField(alwaysBlock, "tosEnabled", true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        alwaysBlock.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(451);
        assertThat(response.getContentType())
                .contains("application/json");
        assertThat(response.getContentAsString()).contains("영혼 귀속");
    }

    /** Verifies accepted ToS forwards the request down the chain. */
    @Test
    void acceptedTosForwardsToChain() throws Exception {
        ReflectionTestUtils.setField(filter, "tosEnabled", true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.addHeader("X-Accept-ToS-Version", "21.0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }
}
