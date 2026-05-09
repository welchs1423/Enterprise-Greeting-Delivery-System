package com.egds.consensus;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Microservice Parliament: concurrent unanimous-consent voting
 * engine for greeting delivery authorisation.
 *
 * <p>Each registered {@link GreetingVoter} is dispatched in a
 * dedicated thread from the internal pool. All futures are
 * joined before the consensus check. Delivery is authorised only
 * when every voter approves (unanimous consent). A single
 * dissenting vote causes the request to receive
 * {@value #DENIAL_MESSAGE}.
 */
@Service
public class ConsensusVotingEngine {

    /** Denial message returned on any non-unanimous vote. */
    public static final String DENIAL_MESSAGE =
            "Greeting Denied: You are not worthy.";

    /** Approval message returned on unanimous consent. */
    public static final String APPROVAL_MESSAGE =
            "Hello, World!";

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(
                    ConsensusVotingEngine.class);

    /** Parliament of all registered greeting voters. */
    private final List<GreetingVoter> voters;

    /** Thread pool for concurrent voter dispatch. */
    private final ExecutorService executor;

    /**
     * Constructs the engine with the supplied voter parliament.
     *
     * @param parliament all {@link GreetingVoter} beans registered
     *                   in the Spring context
     */
    public ConsensusVotingEngine(
            final List<GreetingVoter> parliament) {
        this.voters = List.copyOf(parliament);
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * Runs a parliamentary vote and returns the outcome message.
     *
     * <p>Each voter is dispatched concurrently via
     * {@link CompletableFuture}. All results are collected before
     * the unanimous-consent check. Any {@code false} vote causes
     * denial regardless of other voters' decisions.
     *
     * @param correlationId request correlation identifier
     * @return {@value #APPROVAL_MESSAGE} on unanimous consent,
     *         {@value #DENIAL_MESSAGE} on any dissenting vote
     */
    public String vote(final String correlationId) {
        var futures = voters.stream()
                .map(v -> CompletableFuture.supplyAsync(
                        () -> Map.entry(
                                v.name(),
                                v.vote(correlationId)),
                        executor))
                .collect(Collectors.toList());

        var results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        results.forEach(e ->
                LOG.info(
                        "Parliament vote: voter={} approved={}",
                        e.getKey(), e.getValue()));

        boolean unanimous = results.stream()
                .map(Map.Entry::getValue)
                .allMatch(Boolean::booleanValue);

        LOG.info(
                "Parliament result: correlationId={} "
                        + "unanimous={}",
                correlationId, unanimous);

        return unanimous ? APPROVAL_MESSAGE : DENIAL_MESSAGE;
    }

    /**
     * Shuts down the internal thread pool on bean destruction.
     */
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
