package com.egds.metaphysics;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Shared runtime registry that tracks the total number of greeting
 * messages delivered since JVM startup. Consumed by
 * {@link com.egds.core.aspect.ExistentialLoggingAspect} to increment
 * the counter and by {@link SentienceActuator} to derive metaphysical
 * metrics.
 */
@Component
public class ExistentialStateRegistry {

    /** Total greeting deliveries since JVM startup. */
    private final AtomicLong greetingCount = new AtomicLong(0L);

    /**
     * Atomically increments the delivered greeting count by one.
     *
     * @return the updated count after the increment
     */
    public long incrementGreetingCount() {
        return greetingCount.incrementAndGet();
    }

    /**
     * Returns the total number of greetings delivered since startup.
     *
     * @return the current greeting delivery count
     */
    public long getGreetingCount() {
        return greetingCount.get();
    }
}
