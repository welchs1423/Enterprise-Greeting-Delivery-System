package com.egds.tax;

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
 * Unit tests for {@link MayTaxAndHealthInsuranceInterceptor}.
 */
class MayTaxAndHealthInsuranceInterceptorTest {

    /** Interceptor under test. */
    private MayTaxAndHealthInsuranceInterceptor interceptor;

    /** Initialises a fresh interceptor before each test. */
    @BeforeEach
    void setUp() {
        interceptor = new MayTaxAndHealthInsuranceInterceptor();
    }

    /** Verifies that auth endpoints bypass tax settlement. */
    @Test
    void authEndpointBypassesTaxInterceptor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(interceptor.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass tax settlement. */
    @Test
    void actuatorEndpointBypassesTaxInterceptor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(interceptor.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to tax settlement. */
    @Test
    void greetingEndpointIsSubjectToTax() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(interceptor.shouldNotFilter(request)).isFalse();
    }

    /**
     * Verifies that applyTaxSettlement keeps the first half, appends the
     * seizure marker, and appends the tax warning.
     */
    @Test
    void applyTaxSettlementSeizesSecondHalf() {
        String result =
                interceptor.applyTaxSettlement("Hello, World!");
        assertThat(result).startsWith("Hello,");
        assertThat(result).contains(
                MayTaxAndHealthInsuranceInterceptor.SEIZURE_MARKER);
        assertThat(result).contains(
                MayTaxAndHealthInsuranceInterceptor.TAX_WARNING);
        assertThat(result).doesNotContain("World!");
    }

    /**
     * Verifies that an empty body produces only the seizure marker and
     * tax warning with no leading content.
     */
    @Test
    void applyTaxSettlementOnEmptyBodyReturnsMarkersOnly() {
        String result = interceptor.applyTaxSettlement("");
        assertThat(result).isEqualTo(
                MayTaxAndHealthInsuranceInterceptor.SEIZURE_MARKER
                + MayTaxAndHealthInsuranceInterceptor.TAX_WARNING);
    }

    /** Verifies that when disabled, requests pass through unmodified. */
    @Test
    void disabledInterceptorPassesRequestThrough() throws Exception {
        MayTaxAndHealthInsuranceInterceptor disabled =
                new MayTaxAndHealthInsuranceInterceptor() {
                    @Override
                    boolean isTaxEnabled() {
                        return false;
                    }
                };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        disabled.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }
}
