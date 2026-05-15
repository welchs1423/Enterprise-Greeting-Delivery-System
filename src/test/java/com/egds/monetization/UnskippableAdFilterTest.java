package com.egds.monetization;

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
 * Unit tests for {@link UnskippableAdFilter}.
 */
class UnskippableAdFilterTest {

    private UnskippableAdFilter filter;

    /** Initialises a fresh filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new UnskippableAdFilter();
    }

    /** Verifies that authentication endpoints bypass the ad filter. */
    @Test
    void authEndpointBypassesAdFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass the ad filter. */
    @Test
    void actuatorEndpointBypassesAdFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to the ad filter. */
    @Test
    void greetingEndpointIsSubjectToAdFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    /** Verifies premium header is detected correctly. */
    @Test
    void premiumHeaderIsRecognised() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Premium-Subscription", "true");
        assertThat(filter.isPremium(request)).isTrue();
    }

    /** Verifies missing premium header is not treated as premium. */
    @Test
    void missingPremiumHeaderIsNotPremium() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThat(filter.isPremium(request)).isFalse();
    }

    /** Verifies wrong premium header value is not treated as premium. */
    @Test
    void wrongPremiumHeaderValueIsNotPremium() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Premium-Subscription", "yes");
        assertThat(filter.isPremium(request)).isFalse();
    }

    /** Verifies premium subscriber bypasses delay and passes chain. */
    @Test
    void premiumSubscriberPassesThroughWithoutDelay() throws Exception {
        ReflectionTestUtils.setField(filter, "adsEnabled", true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.addHeader("X-Premium-Subscription", "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    /** Verifies non-premium user still reaches chain after ad delay. */
    @Test
    void nonPremiumUserReachesChainAfterDelay() throws Exception {
        UnskippableAdFilter fastFilter = new UnskippableAdFilter() {
            @Override
            void performAdDelay() {
                // skip actual sleep in unit test
            }
        };
        ReflectionTestUtils.setField(fastFilter, "adsEnabled", true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        fastFilter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }
}
