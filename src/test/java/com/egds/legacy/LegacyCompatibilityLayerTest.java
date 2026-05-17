package com.egds.legacy;

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
 * Unit tests for {@link LegacyCompatibilityLayer}.
 */
class LegacyCompatibilityLayerTest {

    /** Filter under test. */
    private LegacyCompatibilityLayer layer;

    /** Initialises a fresh layer before each test. */
    @BeforeEach
    void setUp() {
        layer = new LegacyCompatibilityLayer();
    }

    /** Verifies that authentication endpoints bypass the layer. */
    @Test
    void authEndpointBypassesLayer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/token");
        assertThat(layer.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that actuator endpoints bypass the layer. */
    @Test
    void actuatorEndpointBypassesLayer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/actuator/health");
        assertThat(layer.shouldNotFilter(request)).isTrue();
    }

    /** Verifies that greeting endpoints are subject to conversion. */
    @Test
    void greetingEndpointIsSubjectToConversion() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        assertThat(layer.shouldNotFilter(request)).isFalse();
    }

    /** Verifies that no-conversion path forwards request to chain. */
    @Test
    void noConversionForwardsRequestToChain() throws Exception {
        LegacyCompatibilityLayer neverConverts =
                new LegacyCompatibilityLayer() {
                    @Override
                    boolean isLegacyConversionActive() {
                        return false;
                    }
                };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        neverConverts.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    /** Verifies that conversion produces valid ROWSET XML. */
    @Test
    void conversionProducesLegacyOracleXml() throws Exception {
        LegacyCompatibilityLayer alwaysConverts =
                new LegacyCompatibilityLayer() {
                    @Override
                    boolean isLegacyConversionActive() {
                        return true;
                    }
                };
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/greeting");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        alwaysConverts.doFilter(request, response, chain);

        assertThat(response.getContentType())
                .contains("text/xml");
        assertThat(response.getContentAsString())
                .contains("<ROWSET>")
                .contains("<LEGACY_DATA>")
                .contains("DELAYED");
    }

    /** Verifies toLegacyOracleXml escapes XML special characters. */
    @Test
    void toLegacyOracleXmlEscapesSpecialChars() {
        String input = "{\"msg\":\"<Hello> & World\"}";
        String result = layer.toLegacyOracleXml(input);

        assertThat(result).contains("&lt;Hello&gt; &amp; World");
        assertThat(result).contains("<ROWSET>");
        assertThat(result).contains("</ROWSET>");
        assertThat(result).contains("Oracle Database 9i");
    }

    /** Verifies toLegacyOracleXml wraps plain JSON without escaping. */
    @Test
    void toLegacyOracleXmlWrapsPlainJson() {
        String input = "{\"greeting\":\"Hello, World!\"}";
        String result = layer.toLegacyOracleXml(input);

        assertThat(result).startsWith(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        assertThat(result).contains(input);
    }

    /** Verifies isLegacyConversionActive returns false when disabled. */
    @Test
    void isLegacyConversionActiveReturnsFalseWhenDisabled() {
        LegacyCompatibilityLayer disabled =
                new LegacyCompatibilityLayer() {
                    @Override
                    boolean isLegacyConversionActive() {
                        return false;
                    }
                };
        assertThat(disabled.isLegacyConversionActive()).isFalse();
    }
}
