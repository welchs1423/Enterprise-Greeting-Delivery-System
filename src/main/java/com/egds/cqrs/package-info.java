/**
 * CQRS (Command Query Responsibility Segregation) and Event Sourcing
 * implementation for the EGDS greeting delivery platform.
 *
 * <p>The package is organized into four sub-packages reflecting the
 * strict separation between write and read paths:
 * <ul>
 *   <li>{@code command} — command objects and command handlers that
 *       mutate system state by publishing events.</li>
 *   <li>{@code event} — immutable domain event records that constitute
 *       the authoritative event log (event sourcing store).</li>
 *   <li>{@code projector} — Kafka consumers that subscribe to the event
 *       log and project events into MongoDB materialized views.</li>
 *   <li>{@code query} — read-model documents, repositories, and query
 *       handlers that serve the read path exclusively from MongoDB.</li>
 * </ul>
 *
 * <p>No component on the read path (projector, query) may mutate
 * command-side state. No component on the write path (command,
 * event publisher) may query the MongoDB read model.
 */
package com.egds.cqrs;
