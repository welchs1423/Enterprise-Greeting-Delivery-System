package com.egds.drm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit tests for {@link VendorLockInDrmFilter}.
 */
class VendorLockInDrmFilterTest {

    /** Filter instance under test. */
    private VendorLockInDrmFilter filter;

    /** Initialises a fresh filter instance before each test. */
    @BeforeEach
    void setUp() {
        filter = new VendorLockInDrmFilter();
    }

    /** Verifies that authentication endpoints bypass DRM. */
    @Test
    void authEndpointBypassesDrm() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass DRM. */
    @Test
    void actuatorEndpointBypassesDrm() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to DRM. */
    @Test
    void greetingEndpointIsSubjectToDrm() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    /** Verifies that hasDongleKey returns true for a non-blank header. */
    @Test
    void hasDongleKeyReturnsTrueForValidHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(VendorLockInDrmFilter.DONGLE_HEADER, "ent-key-1");
        assertThat(filter.hasDongleKey(request)).isTrue();
    }

    /** Verifies that hasDongleKey returns false when header is absent. */
    @Test
    void hasDongleKeyReturnsFalseForMissingHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThat(filter.hasDongleKey(request)).isFalse();
    }

    /**
     * Verifies that a request without a dongle key receives a response
     * body watermarked with {@code [UNREGISTERED EVALUATION COPY]}.
     */
    @Test
    void missingDongleKeyWatermarksResponseBody() throws Exception {
        VendorLockInDrmFilter alwaysActive =
                new VendorLockInDrmFilter() {
                    @Override
                    boolean isDrmActive() {
                        return true;
                    }
                };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) ->
                ((HttpServletResponse) res).getWriter()
                        .write("Hello, World!");

        alwaysActive.doFilter(request, response, chain);

        assertThat(response.getContentAsString())
                .startsWith(VendorLockInDrmFilter.WATERMARK);
        assertThat(response.getContentAsString())
                .endsWith(VendorLockInDrmFilter.WATERMARK);
        assertThat(response.getContentAsString())
                .contains("Hello, World!");
    }

    /**
     * Verifies that a request with a valid dongle key bypasses
     * watermarking and the chain is invoked once.
     */
    @Test
    void validDongleKeyForwardsToChainWithoutWatermark() throws Exception {
        VendorLockInDrmFilter alwaysActive =
                new VendorLockInDrmFilter() {
                    @Override
                    boolean isDrmActive() {
                        return true;
                    }
                };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        request.addHeader(
                VendorLockInDrmFilter.DONGLE_HEADER, "valid-license-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        alwaysActive.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    /**
     * Verifies that when DRM is inactive the filter always forwards
     * to the chain without inspection.
     */
    @Test
    void inactiveDrmAlwaysForwardsToChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }
}
