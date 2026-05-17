package com.egds.msa;

import static org.assertj.core.api.Assertions.assertThat;

import com.egds.web.dto.GreetingResponse;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BloatedComplianceWrapper}.
 */
class BloatedComplianceWrapperTest {

    /** Verifies wrapped payload preserves correlationId. */
    @Test
    void wrap_preservesCorrelationId() {
        BloatedComplianceWrapper wrapper = new BloatedComplianceWrapper();
        GreetingResponse response = new GreetingResponse("test-id-123");

        BloatedCompliancePayload payload = wrapper.wrap(response);

        assertThat(payload.getCorrelationId()).isEqualTo("test-id-123");
    }

    /** Verifies wrapped payload preserves status field. */
    @Test
    void wrap_preservesStatus() {
        BloatedComplianceWrapper wrapper = new BloatedComplianceWrapper();
        GreetingResponse response = new GreetingResponse("c-1");

        BloatedCompliancePayload payload = wrapper.wrap(response);

        assertThat(payload.getStatus()).isEqualTo("ACCEPTED");
    }

    /** Verifies wrapped payload contains non-empty metadata. */
    @Test
    void wrap_metadataIsNotEmpty() {
        BloatedComplianceWrapper wrapper = new BloatedComplianceWrapper();
        GreetingResponse response = new GreetingResponse("c-2");

        BloatedCompliancePayload payload = wrapper.wrap(response);

        assertThat(payload.getMetadata()).isNotEmpty();
    }

    /** Verifies metadata contains carbon emissions key. */
    @Test
    void wrap_metadataContainsCarbonEmissions() {
        BloatedComplianceWrapper wrapper = new BloatedComplianceWrapper();
        GreetingResponse response = new GreetingResponse("c-3");

        BloatedCompliancePayload payload = wrapper.wrap(response);

        assertThat(payload.getMetadata())
                .containsKey("carbonEmissionsGramsCo2Eq");
    }

    /** Verifies metadata contains GDPR disclaimer key. */
    @Test
    void wrap_metadataContainsGdprDisclaimer() {
        BloatedComplianceWrapper wrapper = new BloatedComplianceWrapper();
        GreetingResponse response = new GreetingResponse("c-4");

        BloatedCompliancePayload payload = wrapper.wrap(response);

        assertThat(payload.getMetadata())
                .containsKey("gdprDisclaimer");
    }

    /** Verifies metadata map is immutable. */
    @Test
    void wrap_metadataIsImmutable() {
        BloatedComplianceWrapper wrapper = new BloatedComplianceWrapper();
        GreetingResponse response = new GreetingResponse("c-5");

        BloatedCompliancePayload payload = wrapper.wrap(response);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> payload.getMetadata().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
