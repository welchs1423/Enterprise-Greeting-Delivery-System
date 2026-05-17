package com.egds.frontend;

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
 * Unit tests for {@link NextGenStackBannerFilter}.
 */
class NextGenStackBannerFilterTest {

    /** Filter under test. */
    private NextGenStackBannerFilter filter;

    /** Initialises a fresh filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new NextGenStackBannerFilter();
    }

    /** Verifies that authentication endpoints bypass the filter. */
    @Test
    void authEndpointBypassesFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass the filter. */
    @Test
    void actuatorEndpointBypassesFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to header injection. */
    @Test
    void greetingEndpointIsSubjectToHeaderInjection() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    /** Verifies that the NextGen header is injected when enabled. */
    @Test
    void bannerEnabledInjectsNextGenHeader() throws Exception {
        NextGenStackBannerFilter bannerOn =
                new NextGenStackBannerFilter() {
                    @Override
                    boolean isBannerEnabled() {
                        return true;
                    }
                };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        bannerOn.doFilter(request, response, chain);

        assertThat(response.getHeader(
                NextGenStackBannerFilter.NEXTGEN_HEADER))
                .isEqualTo(NextGenStackBannerFilter.NEXTGEN_VALUE);
        verify(chain, times(1)).doFilter(request, response);
    }

    /** Verifies that no header is injected when the banner is disabled. */
    @Test
    void bannerDisabledSkipsHeaderInjection() throws Exception {
        NextGenStackBannerFilter bannerOff =
                new NextGenStackBannerFilter() {
                    @Override
                    boolean isBannerEnabled() {
                        return false;
                    }
                };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        bannerOff.doFilter(request, response, chain);

        assertThat(response.getHeader(
                NextGenStackBannerFilter.NEXTGEN_HEADER))
                .isNull();
        verify(chain, times(1)).doFilter(request, response);
    }

    /** Verifies that the header value is exactly Vue3-Ready. */
    @Test
    void headerValueIsVue3Ready() {
        assertThat(NextGenStackBannerFilter.NEXTGEN_VALUE)
                .isEqualTo("Vue3-Ready");
    }

    /** Verifies isBannerEnabled delegates to the field. */
    @Test
    void isBannerEnabledReturnsFalseWhenDisabled() {
        NextGenStackBannerFilter disabled =
                new NextGenStackBannerFilter() {
                    @Override
                    boolean isBannerEnabled() {
                        return false;
                    }
                };
        assertThat(disabled.isBannerEnabled()).isFalse();
    }
}
