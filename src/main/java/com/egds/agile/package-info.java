/**
 * Agile process enforcement layer (Phase 14).
 *
 * <p>Provides ceremony-driven pipeline interceptors that inject
 * mandatory scrum rituals into the EGDS delivery flow.
 *
 * <p>Key components:
 * <ul>
 *   <li>{@link com.egds.agile.ScrumMasterDaemon} — blocks the delivery
 *       thread for a daily standup pause and logs standup prompts</li>
 * </ul>
 */
package com.egds.agile;
