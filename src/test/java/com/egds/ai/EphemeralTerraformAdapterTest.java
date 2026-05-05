package com.egds.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link EphemeralTerraformAdapter}.
 *
 * <p>Verifies the mock Terraform lifecycle: provision, invoke, and destroy
 * behave as specified, with correct ARN formatting and deployment tracking.
 */
class EphemeralTerraformAdapterTest {

    private EphemeralTerraformAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EphemeralTerraformAdapter();
    }

    @Test
    void provision_returnsWellFormedLambdaArn() {
        String correlationId = "550e8400-e29b-41d4-a716-446655440000";
        String arn = adapter.provision(correlationId);

        assertNotNull(arn);
        assertTrue(arn.startsWith("arn:aws:lambda:"),
                "ARN must begin with arn:aws:lambda:");
        assertTrue(arn.contains("egds-ephemeral-"),
                "ARN must contain function name prefix");
    }

    @Test
    void provision_incrementsActiveDeploymentCount() {
        assertEquals(0, adapter.activeDeploymentCount());

        adapter.provision("corr-id-1");
        assertEquals(1, adapter.activeDeploymentCount());

        adapter.provision("corr-id-2");
        assertEquals(2, adapter.activeDeploymentCount());
    }

    @Test
    void invoke_returnsGreetingUnchanged() {
        String arn = adapter.provision("corr-id-invoke");
        String greeting = "Hello, World!";

        String result = adapter.invoke(arn, greeting);

        assertEquals(greeting, result,
                "Mock Lambda invocation must return the payload unchanged");
    }

    @Test
    void destroy_removesDeploymentFromActiveSet() {
        String correlationId = "corr-id-destroy";
        adapter.provision(correlationId);
        assertEquals(1, adapter.activeDeploymentCount());

        adapter.destroy(correlationId);

        assertEquals(0, adapter.activeDeploymentCount());
    }

    @Test
    void destroy_unknownCorrelationId_doesNotThrow() {
        adapter.destroy("nonexistent-id");
        assertEquals(0, adapter.activeDeploymentCount());
    }

    @Test
    void fullLifecycle_provisionInvokeDestroy_leavesNoActiveDeployments() {
        String correlationId = "corr-lifecycle";
        String arn = adapter.provision(correlationId);
        adapter.invoke(arn, "Hello, World!");
        adapter.destroy(correlationId);

        assertEquals(0, adapter.activeDeploymentCount(),
                "No active deployments must remain after destroy");
    }
}
