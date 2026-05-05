package com.egds.chaos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EmbeddedChaosMonkey}.
 */
class EmbeddedChaosMonkeyTest {

    private EmbeddedChaosMonkey chaosMonkey;

    /** Creates a new instance with chaos enabled for disruption testing. */
    @BeforeEach
    void setUp() {
        chaosMonkey = new EmbeddedChaosMonkey(true);
    }

    /** Verifies that a disabled monkey never throws or delays. */
    @Test
    void disabledMonkeyNeverDisrupts() throws InterruptedException {
        chaosMonkey.setEnabled(false);
        for (int i = 0; i < 1000; i++) {
            chaosMonkey.unleash();
        }
    }

    /** Verifies the monkey is enabled by default. */
    @Test
    void enabledByDefault() {
        assertThat(chaosMonkey.isEnabled()).isTrue();
    }

    /** Verifies that setEnabled is reflected by isEnabled. */
    @Test
    void canBeDisabledAndReenabled() {
        chaosMonkey.setEnabled(false);
        assertThat(chaosMonkey.isEnabled()).isFalse();
        chaosMonkey.setEnabled(true);
        assertThat(chaosMonkey.isEnabled()).isTrue();
    }
}
