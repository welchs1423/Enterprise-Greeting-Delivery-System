/**
 * Compliance and labor-law enforcement layer for EGDS.
 *
 * <p>Provides two enforcement mechanisms:
 * <ol>
 *   <li>{@link com.egds.compliance.ThreadPerformanceEvaluator} —
 *       measures pipeline thread latency in nanoseconds and issues a
 *       forced interrupt to threads that exceed the threshold.</li>
 *   <li>{@link com.egds.compliance.LaborLawShutdownHook} — tracks
 *       cumulative system uptime and forces all Resilience4j circuit
 *       breakers open when the statutory weekly limit is reached.</li>
 * </ol>
 *
 * <p>{@link com.egds.compliance.LaborLawViolationException} signals
 * the uptime violation to callers.
 */
package com.egds.compliance;
