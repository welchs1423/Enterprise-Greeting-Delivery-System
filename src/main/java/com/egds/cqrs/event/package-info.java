/**
 * Immutable domain event records that constitute the EGDS event log.
 *
 * <p>{@link com.egds.cqrs.event.GreetingRequestedEvent} is the
 * authoritative event published to the Kafka event-sourcing topic
 * {@code egds.greeting.requested} whenever a delivery command is
 * accepted. Events are append-only and must never be mutated after
 * publication.
 */
package com.egds.cqrs.event;
