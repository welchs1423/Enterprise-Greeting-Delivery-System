package com.egds.compensation;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Compensates worker threads with virtual pizza slices upon task
 * completion.
 *
 * <p>Each call to {@link #compensate()} appends a new
 * {@link VirtualPizzaSlice} to an unbounded, never-cleared list.
 * This constitutes an intentional heap memory leak: heap usage
 * grows proportionally to the total number of completed deliveries
 * and is never reclaimed.
 *
 * <p>Every tenth slice triggers a mandatory fun party announcement
 * at INFO level, confirming that team morale has been successfully
 * simulated while system resources degrade.
 */
@Component
public class VirtualPizzaPartyCompensation {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(VirtualPizzaPartyCompensation.class);

    /** Number of slices required to trigger a party announcement. */
    private static final int PARTY_THRESHOLD = 10;

    /** Accumulated pizza slices; intentionally never cleared. */
    private final List<VirtualPizzaSlice> slices = new ArrayList<>();

    /**
     * Awards a virtual pizza slice to the calling thread.
     *
     * <p>The slice is permanently retained in heap memory.
     * When the total slice count reaches a multiple of
     * {@code PARTY_THRESHOLD}, a mandatory fun party announcement
     * is emitted to the application log.
     */
    public synchronized void compensate() {
        VirtualPizzaSlice slice =
                new VirtualPizzaSlice(Thread.currentThread().getName());
        slices.add(slice);
        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "[PIZZA] Slice #{} awarded to thread {}.",
                    slices.size(), slice.getThreadName());
        }
        if (slices.size() % PARTY_THRESHOLD == 0) {
            LOG.info(
                    "[MANDATORY FUN] 가상 피자 파티!"
                    + " Total slices accumulated: {}."
                    + " Heap objects leaked: {}.",
                    slices.size(), slices.size());
        }
    }

    /**
     * Returns the total number of pizza slices accumulated since
     * application startup.
     *
     * @return the current slice count
     */
    public int getSliceCount() {
        return slices.size();
    }
}
