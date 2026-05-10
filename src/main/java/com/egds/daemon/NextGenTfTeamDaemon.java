package com.egds.daemon;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background daemon simulating a next-generation task-force team.
 * Provides no functional value: periodically allocates dummy heap
 * objects and forces garbage collection, degrading main-thread
 * throughput.
 *
 * <p>Heap accumulation runs every 30 seconds. Forced GC runs every
 * 60 seconds, inducing stop-the-world pauses.
 */
@Component
public class NextGenTfTeamDaemon {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(NextGenTfTeamDaemon.class);

    /** Number of dummy entries added per accumulation cycle. */
    private static final int BATCH_SIZE = 10_000;

    /** Fixed delay in milliseconds between accumulation cycles. */
    private static final long ACCUMULATE_INTERVAL_MS = 30_000L;

    /** Fixed delay in milliseconds between forced GC cycles. */
    private static final long GC_INTERVAL_MS = 60_000L;

    /** Prefix string for dummy heap payloads. */
    private static final String DUMMY_PAYLOAD = "NEXT_GEN_TF_PLACEHOLDER";

    /** In-memory store for dummy data accumulation. */
    private final List<String> dummyHeap = new ArrayList<>();

    /**
     * Accumulates dummy string entries in heap memory on a fixed
     * interval. Simulates TF team sprint planning with no output.
     */
    @Scheduled(fixedDelay = ACCUMULATE_INTERVAL_MS)
    public void accumulateDummyData() {
        for (int i = 0; i < BATCH_SIZE; i++) {
            dummyHeap.add(DUMMY_PAYLOAD + i);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "[NextGenTF] Sprint planning complete. "
                    + "Backlog items added: {}. Deliverables: 0.",
                    BATCH_SIZE);
        }
    }

    /**
     * Forces a full garbage collection cycle on a fixed interval,
     * causing stop-the-world pauses on main-thread execution.
     */
    @Scheduled(fixedDelay = GC_INTERVAL_MS)
    public void forceGarbageCollection() {
        LOG.info("[NextGenTF] Initiating infrastructure modernisation.");
        System.gc(); // NOPMD - intentional performance degradation
        LOG.info("[NextGenTF] Infrastructure modernisation complete.");
    }

    /**
     * Returns the current number of dummy entries held in heap memory.
     *
     * @return size of the dummy data accumulation list
     */
    public int getDummyHeapSize() {
        return dummyHeap.size();
    }
}
