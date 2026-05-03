/**
 * CQRS read path: MongoDB read models, repositories, and query handlers.
 *
 * <p>All components in this package operate exclusively against the
 * MongoDB materialized-view collection populated by
 * {@link com.egds.cqrs.projector.GreetingProjector}. No component here
 * may publish Kafka events or invoke command handlers.
 */
package com.egds.cqrs.query;
