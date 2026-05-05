package com.egds.consensus;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsensusVotingEngine")
class ConsensusVotingEngineTest {

    private static final String VALID_UUID =
            "550e8400-e29b-41d4-a716-446655440000";

    @Test
    @DisplayName("unanimous YES produces approval message")
    void unanimousApprovalReturnsApprovalMessage() {
        ConsensusVotingEngine engine =
                new ConsensusVotingEngine(
                        List.of(
                                new AlwaysApproveVoter(),
                                new AlwaysApproveVoter()));
        assertThat(engine.vote(VALID_UUID))
                .isEqualTo(
                        ConsensusVotingEngine.APPROVAL_MESSAGE);
    }

    @Test
    @DisplayName("single NO vote produces denial message")
    void singleDenyVoteReturnsDenialMessage() {
        ConsensusVotingEngine engine =
                new ConsensusVotingEngine(
                        List.of(
                                new AlwaysApproveVoter(),
                                new AlwaysDenyVoter()));
        assertThat(engine.vote(VALID_UUID))
                .isEqualTo(
                        ConsensusVotingEngine.DENIAL_MESSAGE);
    }

    @Test
    @DisplayName("all NO votes produce denial message")
    void allDenyVotesReturnDenialMessage() {
        ConsensusVotingEngine engine =
                new ConsensusVotingEngine(
                        List.of(
                                new AlwaysDenyVoter(),
                                new AlwaysDenyVoter(),
                                new AlwaysDenyVoter()));
        assertThat(engine.vote(VALID_UUID))
                .isEqualTo(
                        ConsensusVotingEngine.DENIAL_MESSAGE);
    }

    @Test
    @DisplayName("BlockchainVerifierVoter approves valid UUID")
    void blockchainVerifierApprovesValidUuid() {
        ConsensusVotingEngine engine =
                new ConsensusVotingEngine(
                        List.of(new BlockchainVerifierVoter()));
        assertThat(engine.vote(VALID_UUID))
                .isEqualTo(
                        ConsensusVotingEngine.APPROVAL_MESSAGE);
    }

    @Test
    @DisplayName("IpfsResolverVoter denies uppercase-start ID")
    void ipfsResolverDeniesUppercaseStartId() {
        ConsensusVotingEngine engine =
                new ConsensusVotingEngine(
                        List.of(new IpfsResolverVoter()));
        assertThat(engine.vote("XXXXXXXX"))
                .isEqualTo(
                        ConsensusVotingEngine.DENIAL_MESSAGE);
    }

    private static final class AlwaysApproveVoter
            implements GreetingVoter {
        @Override
        public String name() {
            return "AlwaysApprove";
        }

        @Override
        public boolean vote(final String id) {
            return true;
        }
    }

    private static final class AlwaysDenyVoter
            implements GreetingVoter {
        @Override
        public String name() {
            return "AlwaysDeny";
        }

        @Override
        public boolean vote(final String id) {
            return false;
        }
    }
}
