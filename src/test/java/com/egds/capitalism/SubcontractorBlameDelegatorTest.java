package com.egds.capitalism;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SubcontractorBlameDelegator}.
 */
class SubcontractorBlameDelegatorTest {

    /**
     * Verifies that the World component is returned when the delegator
     * does not fail.
     */
    @Test
    void returnsWorldComponentOnSuccess() {
        SubcontractorBlameDelegator neverFails =
                new SubcontractorBlameDelegator() {
            @Override
            boolean shouldFail() {
                return false;
            }
        };
        assertThat(neverFails.provideWorldComponent())
                .isEqualTo("World");
    }

    /**
     * Verifies that {@link SubcontractorFailureException} is thrown
     * when the delegator fails.
     */
    @Test
    void throwsSubcontractorFailureExceptionOnFailure() {
        SubcontractorBlameDelegator alwaysFails =
                new SubcontractorBlameDelegator() {
            @Override
            boolean shouldFail() {
                return true;
            }
        };
        assertThatThrownBy(alwaysFails::provideWorldComponent)
                .isInstanceOf(SubcontractorFailureException.class);
    }

    /**
     * Verifies that repeated success calls consistently return "World".
     */
    @Test
    void repeatedSuccessCallsReturnWorld() {
        SubcontractorBlameDelegator stable =
                new SubcontractorBlameDelegator() {
            @Override
            boolean shouldFail() {
                return false;
            }
        };
        for (int i = 0; i < 10; i++) {
            assertThat(stable.provideWorldComponent())
                    .isEqualTo("World");
        }
    }
}
