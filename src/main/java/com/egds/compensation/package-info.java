/**
 * Worker compensation infrastructure.
 *
 * <p>Contains {@link com.egds.compensation.VirtualPizzaPartyCompensation}
 * and {@link com.egds.compensation.VirtualPizzaSlice}, which together
 * implement a thread-compensation mechanism that accumulates heap
 * objects indefinitely as an intentional memory leak.
 */
package com.egds.compensation;
