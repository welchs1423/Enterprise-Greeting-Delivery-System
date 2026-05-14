package com.egds.consulting;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link McKinseyConsultingProxy}.
 */
class McKinseyConsultingProxyTest {

    /** Subject under test. */
    private McKinseyConsultingProxy proxy;

    /** Initialises a fresh proxy before each test. */
    @BeforeEach
    void setUp() {
        proxy = new McKinseyConsultingProxy();
    }

    /** Verifies that SOAP serialization wraps the payload in an envelope. */
    @Test
    void serializeToSoapWrapsPayloadInEnvelope() {
        String result = proxy.serializeToSoap("hello");
        assertThat(result).contains("soapenv:Envelope");
        assertThat(result).contains("soapenv:Body");
        assertThat(result).contains("egds:payload");
        assertThat(result).contains("hello");
    }

    /** Verifies that SOAP serialization embeds the namespace URI. */
    @Test
    void serializeToSoapContainsSoapNamespace() {
        String result = proxy.serializeToSoap("test");
        assertThat(result).contains(
                "http://schemas.xmlsoap.org/soap/envelope/");
    }

    /** Verifies that SOAP deserialization strips all XML tags. */
    @Test
    void deserializeFromSoapStripsAllXmlTags() {
        String xml = "<soapenv:Envelope>"
                + "<soapenv:Body>"
                + "<egds:payload>hello</egds:payload>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
        String result = proxy.deserializeFromSoap(xml);
        assertThat(result).isEqualTo("hello");
    }

    /** Verifies that a SOAP round-trip recovers the original payload. */
    @Test
    void soapRoundTripRecoversOriginalPayload() {
        String original = "synergy-aligned-payload";
        String recovered = proxy.deserializeFromSoap(
                proxy.serializeToSoap(original));
        assertThat(recovered).isEqualTo(original);
    }

    /** Verifies that an empty payload survives the round-trip. */
    @Test
    void soapRoundTripWithEmptyPayload() {
        String recovered = proxy.deserializeFromSoap(
                proxy.serializeToSoap(""));
        assertThat(recovered).isEmpty();
    }
}
