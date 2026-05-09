package com.egds.chaos;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kafka chaos engineering component that stochastically drops outbound
 * events before they reach the broker.
 *
 * <p>On each {@link #shouldDrop(String)} call there is a configurable
 * probability (default 10%) that the method returns {@code true},
 * signaling the publisher to silently discard the event.
 * This exercises the Resilience4j retry and event-sourcing recovery
 * paths under realistic partial-failure conditions.
 *
 * <p>Set {@code egds.chaos.kafka.drop-probability=0.0} in test
 * properties to disable random drops during integration tests.
 */
@Component
public class KafkaChaosMonkey {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(KafkaChaosMonkey.class);

    /** Probability [0.0, 1.0] that any given event is dropped. */
    private final double dropProbability;

    /**
     * @param probability drop probability in [0.0, 1.0]; controlled by
     *                    {@code egds.chaos.kafka.drop-probability}
     */
    public KafkaChaosMonkey(
            @Value("${egds.chaos.kafka.drop-probability:0.10}")
            final double probability) {
        this.dropProbability = probability;
    }

    /**
     * Determines whether the event should be silently dropped before
     * broker submission. A drop is signaled with probability
     * {@link #dropProbability}.
     *
     * @param correlationId the correlation identifier of the event;
     *                      used only for log attribution
     * @return {@code true} if the event must be dropped, {@code false}
     *         if it must be forwarded to the Kafka broker
     */
    public boolean shouldDrop(final String correlationId) {
        boolean drop = ThreadLocalRandom.current().nextDouble()
                < dropProbability;
        if (drop) {
            LOG.warn("[CHAOS] KafkaChaosMonkey dropped event."
                    + " correlationId={}", correlationId);
        }
        return drop;
    }
}
