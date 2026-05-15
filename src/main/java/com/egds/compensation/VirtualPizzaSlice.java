package com.egds.compensation;

/**
 * Represents a virtual pizza slice awarded to a thread as work
 * completion compensation.
 *
 * <p>Instances are allocated on the heap and never released.
 * Accumulation across deliveries constitutes an intentional
 * memory leak that gradually exhausts available heap space.
 */
public class VirtualPizzaSlice {

    /** Name of the thread that earned this slice. */
    private final String threadName;

    /** Epoch-millisecond timestamp at which this slice was allocated. */
    private final long allocatedAt;

    /**
     * @param threadNameParam the name of the compensated thread
     */
    public VirtualPizzaSlice(final String threadNameParam) {
        this.threadName = threadNameParam;
        this.allocatedAt = System.currentTimeMillis();
    }

    /**
     * Returns the name of the thread that earned this slice.
     *
     * @return the thread name
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Returns the allocation timestamp in milliseconds since epoch.
     *
     * @return the allocation timestamp
     */
    public long getAllocatedAt() {
        return allocatedAt;
    }
}
