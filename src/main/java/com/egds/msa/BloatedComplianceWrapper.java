package com.egds.msa;

import com.egds.web.dto.GreetingResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * V22 bloated compliance payload factory.
 *
 * <p>Wraps any {@link GreetingResponse} in a
 * {@link BloatedCompliancePayload} by appending a {@code metadata}
 * node. The node contains:
 * <ul>
 *   <li>Fake deployment environment identifiers</li>
 *   <li>Simulated carbon-emission figures (gCO₂eq per request)</li>
 *   <li>GDPR article-13 disclaimer boilerplate</li>
 *   <li>Fictional SLA guarantee and compliance framework labels</li>
 *   <li>Hardcoded JIRA incident reference</li>
 * </ul>
 *
 * <p>All metadata values are hard-coded fabrications. No real
 * environment variables are read; doing so could expose secrets.
 */
@Component
public class BloatedComplianceWrapper {

    /**
     * Wraps the supplied greeting response in a compliance payload.
     *
     * @param response the original greeting response to wrap
     * @return a {@link BloatedCompliancePayload} containing the
     *         original fields plus the bloated metadata node
     */
    public BloatedCompliancePayload wrap(
            final GreetingResponse response) {
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("buildVersion", "22.0.0-SNAPSHOT");
        meta.put("deploymentEnvironment",
                "k8s-prod-cluster-euwest1-az3");
        meta.put("deploymentRegion", "ap-northeast-2");
        meta.put("replicaId", "egds-pod-7f9b4c-xk8pz");
        meta.put("carbonEmissionsGramsCo2Eq", "0.000042");
        meta.put("carbonOffsetCertificate",
                "CERT-EGDS-CARBON-2026-Q2-000042");
        meta.put("complianceFramework",
                "GDPR-2018/SOC2-TYPE2/ISO27001/PCI-DSS-v4");
        meta.put("gdprDisclaimer",
                "This response was processed under GDPR Art. 13. "
                + "Data is retained for 7 years per ISO 27001 "
                + "Annex A.12.3. To exercise your right to "
                + "erasure, submit form EGDS-GDPR-001 in "
                + "triplicate to the DPO.");
        meta.put("slaGuarantee", "99.999%");
        meta.put("incidentTicketResolved",
                "JIRA-EGDS-99999-RESOLVED");
        meta.put("fakeEnvVar1",
                "EGDS_PROD_CLUSTER_ID=euwest1-prod-007");
        meta.put("fakeEnvVar2",
                "EGDS_SIDECAR_PROXY=envoy-v1.29.4");
        meta.put("fakeEnvVar3",
                "EGDS_MESH_VERSION=istio-1.21.0");
        return new BloatedCompliancePayload(
                response.getCorrelationId(),
                response.getStatus(),
                response.getMessage(),
                meta);
    }
}
