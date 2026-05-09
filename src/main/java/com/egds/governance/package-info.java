/**
 * AI board-of-directors unanimous-approval gate (Phase 13).
 *
 * <p>Enforces a bureaucratic approval workflow in which three virtual
 * executives (CEO, CFO, Compliance) must unanimously vote in favour
 * of a greeting delivery before it is permitted to proceed.
 *
 * <p>Key components:
 * <ul>
 *   <li>{@link com.egds.governance.AiBoardApprovalService} — parallel
 *       deliberation and vote aggregation</li>
 * </ul>
 */
package com.egds.governance;
