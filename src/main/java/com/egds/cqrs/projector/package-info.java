/**
 * Event projectors: Kafka consumers that subscribe to the EGDS event
 * log and build MongoDB materialized views (read models).
 *
 * <p>Projectors must be idempotent. Re-processing the same event must
 * produce the same read-model state. No projector may publish to a
 * Kafka topic or invoke a command handler.
 */
package com.egds.cqrs.projector;
