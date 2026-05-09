package com.egds.chaos;

import com.egds.ipfs.IpfsGreetingResolver;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Daemon that simulates cosmic-ray single-event upsets in IPFS storage.
 *
 * <p>Runs on a single daemon thread with a randomised delay between
 * {@value #MIN_DELAY_MS} and {@value #MAX_DELAY_MS} milliseconds. Each
 * tick selects a random CID in {@link IpfsGreetingResolver}, flips one
 * bit in one of its ECC bytes, and reschedules itself.
 *
 * <p>Because the stored bytes are Hamming(7,4) encoded, a single-bit
 * error per byte is always correctable by
 * {@link EccRecoveryFilter#decode} at resolve time.
 */
@Component
public class CosmicRaySimulator {

    /** Logger for this component. */
    private static final Logger LOG =
        LoggerFactory.getLogger(CosmicRaySimulator.class);

    /** Minimum inter-event delay in milliseconds. */
    private static final int MIN_DELAY_MS = 3000;

    /** Maximum inter-event delay in milliseconds. */
    private static final int MAX_DELAY_MS = 8000;

    /** Number of valid bit positions within a 7-bit Hamming codeword. */
    private static final int HAMMING_BIT_COUNT = 7;

    /** Number of CID characters logged per event for brevity. */
    private static final int CID_LOG_PREFIX_LEN = 8;

    /** IPFS store whose ECC bytes are corrupted by this daemon. */
    private final IpfsGreetingResolver resolver;

    /** Executor running the single daemon simulation thread. */
    private final ScheduledExecutorService executor;

    /** Cumulative count of bit-flip events since startup. */
    private final AtomicLong hitCount = new AtomicLong(0);

    /**
     * Constructs the simulator and starts the daemon thread.
     *
     * @param ipfsResolver the IPFS store whose ECC bytes will be corrupted
     */
    public CosmicRaySimulator(final IpfsGreetingResolver ipfsResolver) {
        this.resolver = ipfsResolver;
        this.executor = Executors.newSingleThreadScheduledExecutor(
            r -> {
                Thread t = new Thread(r, "cosmic-ray-daemon");
                t.setDaemon(true);
                return t;
            });
        scheduleNext();
    }

    /**
     * Returns the total count of bit-flip events fired since startup.
     *
     * @return cumulative hit count
     */
    public long getHitCount() {
        return hitCount.get();
    }

    /**
     * Shuts down the daemon executor gracefully.
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * Flips one random bit in the ECC bytes stored under the given CID.
     * If the CID is absent or its byte array is empty, the call is a no-op.
     *
     * @param cid content identifier of the target entry
     */
    void flipRandomBit(final String cid) {
        byte[] eccBytes = resolver.getRawEccBytes(cid);
        if (eccBytes == null || eccBytes.length == 0) {
            return;
        }
        int byteIdx = ThreadLocalRandom.current()
            .nextInt(eccBytes.length);
        int bitIdx = ThreadLocalRandom.current()
            .nextInt(HAMMING_BIT_COUNT);
        eccBytes[byteIdx] ^= (byte) (1 << bitIdx);
        resolver.putRawEccBytes(cid, eccBytes);
        hitCount.incrementAndGet();
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "Cosmic ray: bit {} flipped in byte {} of CID {}",
                bitIdx, byteIdx,
                cid.substring(0, CID_LOG_PREFIX_LEN));
        }
    }

    private void scheduleNext() {
        int delay = ThreadLocalRandom.current()
            .nextInt(MIN_DELAY_MS, MAX_DELAY_MS);
        executor.schedule(this::tick, delay, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void tick() {
        try {
            String[] cids = resolver.listCids();
            if (cids.length == 0) {
                return;
            }
            String target = cids[
                ThreadLocalRandom.current().nextInt(cids.length)];
            flipRandomBit(target);
        } finally {
            scheduleNext();
        }
    }
}
