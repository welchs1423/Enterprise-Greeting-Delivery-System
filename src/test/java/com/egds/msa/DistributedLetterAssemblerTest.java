package com.egds.msa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link DistributedLetterAssembler}.
 */
class DistributedLetterAssemblerTest {

    /** Verifies disabled assembler returns input unchanged. */
    @Test
    void disabledAssembler_returnsInputUnchanged() {
        EnterpriseChaosMonkey monkey = mock(EnterpriseChaosMonkey.class);
        DistributedLetterAssembler assembler =
                new DistributedLetterAssembler(monkey);
        ReflectionTestUtils.setField(
                assembler, "assemblerEnabled", false);

        String result = assembler.assemble("Hello");
        assertThat(result).isEqualTo("Hello");
        verify(monkey, never()).sabotage();
    }

    /** Verifies disabled assembler returns null unchanged. */
    @Test
    void disabledAssembler_returnsNullUnchanged() {
        EnterpriseChaosMonkey monkey = mock(EnterpriseChaosMonkey.class);
        DistributedLetterAssembler assembler =
                new DistributedLetterAssembler(monkey);
        ReflectionTestUtils.setField(
                assembler, "assemblerEnabled", false);

        assertThat(assembler.assemble(null)).isNull();
    }

    /** Verifies enabled assembler with no-op monkey preserves text. */
    @Test
    void enabledAssembler_noOpMonkey_preservesText() {
        EnterpriseChaosMonkey monkey = new EnterpriseChaosMonkey();
        monkey.setChaosEnabled(false);
        DistributedLetterAssembler assembler =
                new DistributedLetterAssembler(monkey);
        ReflectionTestUtils.setField(
                assembler, "assemblerEnabled", true);

        String input = "Hi";
        String result = assembler.assemble(input);
        assertThat(result).hasSize(input.length());
    }

    /** Verifies fetchLetter returns space when chaos monkey throws. */
    @Test
    void fetchLetter_chaosThrows_returnsSpace() {
        EnterpriseChaosMonkey alwaysThrows = new EnterpriseChaosMonkey() {
            @Override
            public void sabotage() {
                throw new RuntimeException("forced chaos");
            }
        };
        alwaysThrows.setChaosEnabled(true);
        DistributedLetterAssembler assembler =
                new DistributedLetterAssembler(alwaysThrows);
        ReflectionTestUtils.setField(
                assembler, "assemblerEnabled", true);

        char result = assembler.fetchLetter('A');
        assertThat(result).isEqualTo(' ');
    }

    /** Verifies fetchLetter returns the original char when no chaos. */
    @Test
    void fetchLetter_noChoas_returnsOriginalChar() {
        EnterpriseChaosMonkey noop = new EnterpriseChaosMonkey();
        noop.setChaosEnabled(false);
        DistributedLetterAssembler assembler =
                new DistributedLetterAssembler(noop);

        char result = assembler.fetchLetter('Z');
        assertThat(result).isEqualTo('Z');
    }
}
