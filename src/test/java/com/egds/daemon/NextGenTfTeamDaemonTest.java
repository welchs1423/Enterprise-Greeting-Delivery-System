package com.egds.daemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link NextGenTfTeamDaemon}.
 */
class NextGenTfTeamDaemonTest {

    private NextGenTfTeamDaemon daemon;

    /** Initialises a fresh daemon before each test. */
    @BeforeEach
    void setUp() {
        daemon = new NextGenTfTeamDaemon();
    }

    /** Verifies that the dummy heap starts empty on initialisation. */
    @Test
    void initialHeapSizeIsZero() {
        assertThat(daemon.getDummyHeapSize()).isZero();
    }

    /** Verifies that one accumulation cycle adds exactly 10000 entries. */
    @Test
    void accumulateDummyDataIncreasesHeapByBatchSize() {
        daemon.accumulateDummyData();
        assertThat(daemon.getDummyHeapSize()).isEqualTo(10_000);
    }

    /** Verifies that multiple cycles grow the heap linearly. */
    @Test
    void multipleAccumulationsGrowHeapLinearly() {
        daemon.accumulateDummyData();
        daemon.accumulateDummyData();
        assertThat(daemon.getDummyHeapSize()).isEqualTo(20_000);
    }

    /** Verifies that forceGarbageCollection completes without error. */
    @Test
    void forceGarbageCollectionDoesNotThrow() {
        assertThatCode(() -> daemon.forceGarbageCollection())
                .doesNotThrowAnyException();
    }
}
