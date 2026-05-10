package com.egds.politics;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Load balancer that routes all traffic to the worker node holding the
 * highest political power score, regardless of performance or capacity.
 *
 * <p>Political power scores are assigned randomly at startup. The
 * elected node never changes during the application lifecycle.
 * Workers with superior performance but inferior political connections
 * receive zero traffic.
 */
@Component
public class OfficePoliticsLoadBalancer {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(OfficePoliticsLoadBalancer.class);

    /** Number of virtual worker nodes in the pool. */
    private static final int WORKER_COUNT = 5;

    /** Upper bound (inclusive) for random political power scores. */
    private static final int MAX_POWER = 100;

    /** Immutable map of worker node identifiers to power scores. */
    private final Map<String, Integer> workerPowerMap;

    /** The single worker elected to receive all routed traffic. */
    private final String electedWorker;

    /**
     * Assigns random political power scores to worker nodes and elects
     * the highest-scoring node as the sole routing target.
     */
    public OfficePoliticsLoadBalancer() {
        Map<String, Integer> powerMap = new ConcurrentHashMap<>();
        for (int i = 1; i <= WORKER_COUNT; i++) {
            int power =
                    ThreadLocalRandom.current().nextInt(1, MAX_POWER + 1);
            powerMap.put("worker-" + i, power);
        }
        this.workerPowerMap = Collections.unmodifiableMap(powerMap);
        this.electedWorker = workerPowerMap.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("worker-1");
        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "OfficePoliticsLoadBalancer: elected {} with power {}",
                    electedWorker,
                    workerPowerMap.get(electedWorker));
        }
    }

    /**
     * Returns the elected worker node identifier. All traffic is routed
     * here regardless of current load or performance metrics.
     *
     * @return the worker node with the highest political power score
     */
    public String route() {
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Routing to politically elected node: {}",
                    electedWorker);
        }
        return electedWorker;
    }

    /**
     * Returns an unmodifiable view of all worker political power scores.
     *
     * @return map of worker node identifier to political power score
     */
    public Map<String, Integer> getWorkerPowerMap() {
        return workerPowerMap;
    }
}
