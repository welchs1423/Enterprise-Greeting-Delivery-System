package com.egds.chaos;

import static org.assertj.core.api.Assertions.assertThat;

import com.egds.ipfs.IpfsGreetingResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CosmicRaySimulator} bit-flip injection.
 */
class CosmicRaySimulatorTest {

    private IpfsGreetingResolver resolver;
    private CosmicRaySimulator simulator;

    /** Sets up resolver and simulator; daemon thread starts but won't fire. */
    @BeforeEach
    void setUp() {
        EccRecoveryFilter eccFilter = new EccRecoveryFilter();
        resolver = new IpfsGreetingResolver(eccFilter);
        simulator = new CosmicRaySimulator(resolver);
    }

    /** Shuts down the daemon thread after each test. */
    @AfterEach
    void tearDown() {
        simulator.shutdown();
    }

    /** Verifies that a manual bit flip is counted and ECC corrects it. */
    @Test
    void bitFlipCorruptsButEccRecovers() {
        String cid = resolver.store("Hello, World!");
        simulator.flipRandomBit(cid);
        assertThat(simulator.getHitCount()).isEqualTo(1);
        assertThat(resolver.resolve(cid))
            .isPresent()
            .hasValue("Hello, World!");
    }

    /** Verifies that flipping on an absent CID leaves the counter unchanged. */
    @Test
    void noFlipOnAbsentCid() {
        long before = simulator.getHitCount();
        simulator.flipRandomBit("does-not-exist");
        assertThat(simulator.getHitCount()).isEqualTo(before);
    }

    /** Verifies that the hit counter accumulates across multiple flips. */
    @Test
    void hitCountAccumulates() {
        String cid = resolver.store("Cosmos");
        simulator.flipRandomBit(cid);
        simulator.flipRandomBit(cid);
        assertThat(simulator.getHitCount()).isGreaterThanOrEqualTo(2);
    }
}
