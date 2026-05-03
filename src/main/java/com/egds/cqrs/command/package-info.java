/**
 * CQRS command side: command value objects and their handlers.
 *
 * <p>A command expresses the intent to change system state.
 * {@link com.egds.cqrs.command.GreetingCommandHandler} accepts a
 * {@link com.egds.cqrs.command.DeliverGreetingCommand}, publishes the
 * resulting {@link com.egds.cqrs.event.GreetingRequestedEvent} to the
 * Kafka event log, and delegates to the existing Kafka event publisher
 * for legacy pipeline compatibility.
 */
package com.egds.cqrs.command;
