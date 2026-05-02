/**
 * Distributed tracing integration tests for the EGDS platform (Phase 4).
 *
 * <p>Verifies that the Micrometer Tracing OTel bridge auto-configures
 * correctly, that spans carry non-zero trace identifiers, and that the
 * {@code traceId} MDC key is populated and cleared correctly within
 * span scopes.
 */
package com.egds.observability;
