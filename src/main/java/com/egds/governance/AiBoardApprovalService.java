package com.egds.governance;

import com.egds.core.exception.BoardRejectionException;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Simulates an AI board-of-directors unanimous-approval gate.
 * Three virtual executives (CEO, CFO, Compliance) deliberate in
 * parallel before casting their votes.  All three must approve before
 * a greeting delivery is permitted to proceed.
 *
 * <p>Each deliberation is run in a dedicated thread-pool thread and
 * sleeps for {@link #DEFAULT_DELIBERATION_MS} milliseconds to model
 * executive decision latency.  If any member rejects,
 * {@link BoardRejectionException} is raised.
 */
@Service
public class AiBoardApprovalService {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(AiBoardApprovalService.class);

    /** Default deliberation time per board member in milliseconds. */
    static final long DEFAULT_DELIBERATION_MS = 200L;

    /** Names of the three virtual board members. */
    private static final List<String> BOARD_MEMBERS =
            List.of("CEO", "CFO", "Compliance");

    /** Deliberation pause applied by each board member. */
    private final long deliberationMs;

    /** Thread pool used to run board member deliberations in parallel. */
    private final ExecutorService boardExecutor =
            Executors.newFixedThreadPool(BOARD_MEMBERS.size());

    /**
     * Constructs an {@code AiBoardApprovalService} using the default
     * deliberation delay of {@value #DEFAULT_DELIBERATION_MS} ms.
     */
    public AiBoardApprovalService() {
        this.deliberationMs = DEFAULT_DELIBERATION_MS;
    }

    /**
     * Constructs an {@code AiBoardApprovalService} with a custom
     * deliberation delay.  Intended for use in unit tests.
     *
     * @param deliberationMillis deliberation pause per member in ms
     */
    AiBoardApprovalService(final long deliberationMillis) {
        this.deliberationMs = deliberationMillis;
    }

    /**
     * Blocks until all board members have voted and raises
     * {@link BoardRejectionException} if unanimous approval is not
     * reached.
     *
     * @param correlationId delivery cycle identifier used in audit logs
     * @throws BoardRejectionException if any board member rejects
     */
    public void requestApproval(final String correlationId) {
        LOG.info("[BOARD] Approval requested correlationId={}",
                correlationId);

        List<CompletableFuture<BoardVote>> futures = BOARD_MEMBERS.stream()
                .map(member -> CompletableFuture.supplyAsync(
                        () -> deliberate(member, correlationId),
                        boardExecutor))
                .toList();

        futures.stream()
                .map(CompletableFuture::join)
                .filter(vote -> !vote.approved())
                .findFirst()
                .ifPresent(vote -> {
                    throw new BoardRejectionException(
                            "Board member " + vote.member()
                            + " rejected the greeting proposal.",
                            correlationId,
                            vote.member());
                });

        LOG.info("[BOARD] Unanimous approval reached correlationId={}",
                correlationId);
    }

    /**
     * Simulates one board member's deliberation and returns a vote.
     * Overridable in tests to inject custom vote outcomes.
     *
     * @param member        name of the board member
     * @param correlationId delivery cycle identifier
     * @return the member's vote
     */
    BoardVote deliberate(
            final String member,
            final String correlationId) {
        LOG.info("[BOARD] {} deliberating correlationId={}",
                member, correlationId);
        try {
            Thread.sleep(deliberationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOG.info("[BOARD] {} approved correlationId={}",
                member, correlationId);
        return new BoardVote(member, true);
    }

    /** Shuts down the board deliberation thread pool on context close. */
    @PreDestroy
    void shutdown() {
        boardExecutor.shutdown();
    }

    /**
     * Immutable record representing a single board member's vote.
     *
     * @param member   name of the voting board member
     * @param approved true when the member approves the proposal
     */
    record BoardVote(String member, boolean approved) { }
}
