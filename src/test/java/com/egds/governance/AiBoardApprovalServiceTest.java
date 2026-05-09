package com.egds.governance;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.egds.core.exception.BoardRejectionException;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AiBoardApprovalService}.
 */
class AiBoardApprovalServiceTest {

    /** Verifies that unanimous approval raises no exception. */
    @Test
    void requestApprovalSucceedsOnUnanimousApproval() {
        AiBoardApprovalService service = new AiBoardApprovalService(0L);
        assertThatNoException().isThrownBy(
                () -> service.requestApproval("corr-ok-001"));
    }

    /** Verifies that a single dissenting vote raises BoardRejectionException. */
    @Test
    void requestApprovalThrowsWhenCeoRejects() {
        AiBoardApprovalService dissentingService =
                new AiBoardApprovalService(0L) {
                    @Override
                    AiBoardApprovalService.BoardVote deliberate(
                            final String member,
                            final String correlationId) {
                        if ("CEO".equals(member)) {
                            return new AiBoardApprovalService.BoardVote(
                                    member, false);
                        }
                        return new AiBoardApprovalService.BoardVote(
                                member, true);
                    }
                };

        assertThatThrownBy(
                () -> dissentingService.requestApproval("corr-rej-001"))
                .isInstanceOf(BoardRejectionException.class)
                .hasMessageContaining("CEO");
    }

    /** Verifies that the rejecting member name is carried in the exception. */
    @Test
    void rejectionExceptionContainsRejectingMemberName() {
        AiBoardApprovalService dissentingService =
                new AiBoardApprovalService(0L) {
                    @Override
                    AiBoardApprovalService.BoardVote deliberate(
                            final String member,
                            final String correlationId) {
                        if ("Compliance".equals(member)) {
                            return new AiBoardApprovalService.BoardVote(
                                    member, false);
                        }
                        return new AiBoardApprovalService.BoardVote(
                                member, true);
                    }
                };

        assertThatThrownBy(
                () -> dissentingService.requestApproval("corr-rej-002"))
                .isInstanceOf(BoardRejectionException.class)
                .satisfies(ex -> {
                    BoardRejectionException bre =
                            (BoardRejectionException) ex;
                    org.assertj.core.api.Assertions
                            .assertThat(bre.getRejectingMember())
                            .isEqualTo("Compliance");
                });
    }
}
