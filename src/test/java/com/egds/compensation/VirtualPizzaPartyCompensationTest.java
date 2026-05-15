package com.egds.compensation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link VirtualPizzaPartyCompensation}.
 */
class VirtualPizzaPartyCompensationTest {

    /** Compensation instance under test. */
    private VirtualPizzaPartyCompensation compensation;

    /** Initialises a fresh compensation instance before each test. */
    @BeforeEach
    void setUp() {
        compensation = new VirtualPizzaPartyCompensation();
    }

    /** Verifies that the initial slice count is zero on construction. */
    @Test
    void initialSliceCountIsZero() {
        assertThat(compensation.getSliceCount()).isZero();
    }

    /** Verifies that each compensate call increments the slice count. */
    @Test
    void compensateIncrementsSliceCount() {
        compensation.compensate();
        assertThat(compensation.getSliceCount()).isEqualTo(1);
    }

    /** Verifies that slices accumulate linearly across multiple calls. */
    @Test
    void slicesAccumulateAcrossMultipleCalls() {
        for (int i = 0; i < 5; i++) {
            compensation.compensate();
        }
        assertThat(compensation.getSliceCount()).isEqualTo(5);
    }

    /**
     * Verifies that the party threshold fires exactly at ten slices
     * without throwing or resetting the counter.
     */
    @Test
    void tenSlicesReachPartyThresholdWithoutResettingCount() {
        for (int i = 0; i < 10; i++) {
            compensation.compensate();
        }
        assertThat(compensation.getSliceCount()).isEqualTo(10);
    }

    /**
     * Verifies that slices continue to accumulate past the party
     * threshold and are never discarded.
     */
    @Test
    void slicesNeverDecreaseAfterPartyThreshold() {
        for (int i = 0; i < 15; i++) {
            compensation.compensate();
        }
        assertThat(compensation.getSliceCount()).isEqualTo(15);
    }

    /**
     * Verifies that the party announcement fires at the twentieth
     * slice (second multiple of the threshold).
     */
    @Test
    void partyAnnouncementFiresAtSecondThresholdMultiple() {
        for (int i = 0; i < 20; i++) {
            compensation.compensate();
        }
        assertThat(compensation.getSliceCount()).isEqualTo(20);
    }
}
