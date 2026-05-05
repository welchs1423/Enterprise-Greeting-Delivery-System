package com.egds.temporal;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TemporalRollbackManager")
class TemporalRollbackManagerTest {

    private TemporalRollbackManager manager;

    @BeforeEach
    void setUp() {
        manager = new TemporalRollbackManager();
    }

    @Test
    @DisplayName("register increments pending count by one")
    void registerIncrementsPendingCount() {
        PredictedGreetingEntry entry =
                new PredictedGreetingEntry(
                        "corr-001", "Hello, World!",
                        Instant.now(), false);
        manager.register(entry);
        assertThat(manager.pendingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("claim returns true for registered entry")
    void claimReturnsTrueForRegisteredEntry() {
        PredictedGreetingEntry entry =
                new PredictedGreetingEntry(
                        "corr-002", "Hello, World!",
                        Instant.now(), false);
        manager.register(entry);
        assertThat(manager.claim("corr-002")).isTrue();
    }

    @Test
    @DisplayName("claim returns false for unknown entry")
    void claimReturnsFalseForUnknownEntry() {
        assertThat(manager.claim("does-not-exist")).isFalse();
    }

    @Test
    @DisplayName("rollbackExpired retains fresh entries")
    void rollbackExpiredRetainsFreshEntries() {
        PredictedGreetingEntry entry =
                new PredictedGreetingEntry(
                        "corr-003", "Hello, World!",
                        Instant.now(), false);
        manager.register(entry);
        manager.rollbackExpired();
        assertThat(manager.pendingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("rollbackExpired removes stale unclaimed entries")
    void rollbackExpiredRemovesStaleEntries() {
        Instant ancient = Instant.now().minusSeconds(3600);
        PredictedGreetingEntry stale =
                new PredictedGreetingEntry(
                        "corr-004", "Hello, World!",
                        ancient, false);
        manager.register(stale);
        manager.rollbackExpired();
        assertThat(manager.pendingCount()).isZero();
    }

    @Test
    @DisplayName("rollbackExpired retains claimed stale entries")
    void rollbackExpiredRetainsClaimedStalEntries() {
        Instant ancient = Instant.now().minusSeconds(3600);
        PredictedGreetingEntry stale =
                new PredictedGreetingEntry(
                        "corr-005", "Hello, World!",
                        ancient, false);
        manager.register(stale);
        manager.claim("corr-005");
        manager.rollbackExpired();
        assertThat(manager.pendingCount()).isEqualTo(1);
    }
}
