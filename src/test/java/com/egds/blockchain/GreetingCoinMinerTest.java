package com.egds.blockchain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GreetingCoinMiner}.
 */
class GreetingCoinMinerTest {

    private GreetingCoinMiner miner;

    /** Initializes a fresh miner instance before each test. */
    @BeforeEach
    void setUp() {
        miner = new GreetingCoinMiner();
    }

    /** Verifies that the mined hash starts with the difficulty prefix. */
    @Test
    void mineReturnsHashStartingWithDifficultyPrefix() {
        GreetingCoinMiner.MiningResult result = miner.mine("test-corr-001");
        assertThat(result.hash())
                .startsWith(GreetingCoinMiner.DIFFICULTY_PREFIX);
    }

    /** Verifies that the mined nonce is non-negative. */
    @Test
    void mineNonceIsNonNegative() {
        GreetingCoinMiner.MiningResult result = miner.mine("test-corr-002");
        assertThat(result.nonce()).isGreaterThanOrEqualTo(0L);
    }

    /** Verifies that mining is deterministic for the same seed. */
    @Test
    void mineIsDeterministicForSameSeed() {
        GreetingCoinMiner.MiningResult first = miner.mine("deterministic");
        GreetingCoinMiner.MiningResult second = miner.mine("deterministic");
        assertThat(first.nonce()).isEqualTo(second.nonce());
        assertThat(first.hash()).isEqualTo(second.hash());
    }

    /** Verifies that mining produces different results for different seeds. */
    @Test
    void mineDifferentSeedsProducePotentiallyDifferentResults() {
        GreetingCoinMiner.MiningResult a = miner.mine("seed-alpha");
        GreetingCoinMiner.MiningResult b = miner.mine("seed-beta");
        assertThat(a.hash()).startsWith(GreetingCoinMiner.DIFFICULTY_PREFIX);
        assertThat(b.hash()).startsWith(GreetingCoinMiner.DIFFICULTY_PREFIX);
    }
}
