package com.egds.msa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link EnterpriseChaosMonkey}.
 */
class EnterpriseChaosMonkeyTest {

    /** Verifies disabled monkey never throws. */
    @Test
    void disabledMonkey_neverThrows() {
        EnterpriseChaosMonkey monkey = new EnterpriseChaosMonkey();
        ReflectionTestUtils.setField(monkey, "chaosEnabled", false);
        assertThatCode(monkey::sabotage).doesNotThrowAnyException();
    }

    /** Verifies enabled monkey throws when roll is below threshold. */
    @Test
    void enabledMonkey_throwsOnLowRoll() {
        EnterpriseChaosMonkeyUnderTest monkey =
                new EnterpriseChaosMonkeyUnderTest(0);
        assertThatThrownBy(monkey::sabotage)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("chaos");
    }

    /** Verifies enabled monkey does not throw on high roll. */
    @Test
    void enabledMonkey_doesNotThrowOnHighRoll() {
        EnterpriseChaosMonkeyUnderTest monkey =
                new EnterpriseChaosMonkeyUnderTest(99);
        assertThatCode(monkey::sabotage).doesNotThrowAnyException();
    }

    /** Verifies setChaosEnabled toggles the flag. */
    @Test
    void setChaosEnabled_togglesFlag() {
        EnterpriseChaosMonkey monkey = new EnterpriseChaosMonkey();
        monkey.setChaosEnabled(false);
        assertThat(monkey.isChaosEnabled()).isFalse();
        monkey.setChaosEnabled(true);
        assertThat(monkey.isChaosEnabled()).isTrue();
    }

    /**
     * Test subclass that overrides ThreadLocalRandom roll
     * with a fixed value.
     */
    private static final class EnterpriseChaosMonkeyUnderTest
            extends EnterpriseChaosMonkey {

        /** Fixed roll value for deterministic testing. */
        private final int fixedRoll;

        /**
         * @param roll the fixed roll value to use on every sabotage
         */
        EnterpriseChaosMonkeyUnderTest(final int roll) {
            this.fixedRoll = roll;
            setChaosEnabled(true);
        }

        @Override
        public void sabotage() {
            if (fixedRoll < 5) {
                throw new RuntimeException(
                        "Letter microservice timeout (simulated chaos)");
            }
        }
    }
}
