package com.egds.mainframe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link As400MainframeEmulator}.
 *
 * <p>Verifies EBCDIC encoding correctness, 2PC lifecycle (prepare/commit
 * and prepare/rollback), and ledger read-back behaviour.
 */
class As400MainframeEmulatorTest {

    private static final Charset EBCDIC = Charset.forName("IBM037");

    private As400MainframeEmulator emulator;

    @BeforeEach
    void setUp() {
        emulator = new As400MainframeEmulator();
    }

    @Test
    void prepare_returnsEbcdicEncodedBytes() {
        String greeting = "Hello, World!";
        byte[] result = emulator.prepare("corr-1", greeting);

        byte[] expected = greeting.getBytes(EBCDIC);
        assertArrayEquals(expected, result,
                "prepare must return IBM037 EBCDIC encoding of the greeting");
    }

    @Test
    void prepare_ebcdicDiffersFromUtf8() {
        String greeting = "Hello";
        byte[] ebcdic = emulator.prepare("corr-utf8", greeting);
        byte[] utf8 = greeting.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        boolean differs = false;
        if (ebcdic.length != utf8.length) {
            differs = true;
        } else {
            for (int i = 0; i < ebcdic.length; i++) {
                if (ebcdic[i] != utf8[i]) {
                    differs = true;
                    break;
                }
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(differs,
                "EBCDIC encoding must differ from UTF-8 for ASCII text");
    }

    @Test
    void commit_promotesRecordToLedger() {
        String correlationId = "corr-commit";
        String greeting = "Hello, World!";

        emulator.prepare(correlationId, greeting);
        emulator.commit(correlationId);

        String readBack = emulator.read(correlationId);
        assertNotNull(readBack);
        assertEquals(greeting, readBack,
                "Committed record must round-trip through EBCDIC unchanged");
    }

    @Test
    void commit_incrementsLedgerSize() {
        assertEquals(0, emulator.ledgerSize());

        emulator.prepare("c1", "Hello");
        emulator.commit("c1");
        assertEquals(1, emulator.ledgerSize());

        emulator.prepare("c2", "World");
        emulator.commit("c2");
        assertEquals(2, emulator.ledgerSize());
    }

    @Test
    void commit_withoutPrepare_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> emulator.commit("no-prepare"),
                "Committing an unstaged record must throw");
    }

    @Test
    void rollback_discardsStageWithoutPromotingToLedger() {
        String correlationId = "corr-rollback";
        emulator.prepare(correlationId, "Hello");
        emulator.rollback(correlationId);

        assertNull(emulator.read(correlationId),
                "Rolled-back record must not appear in the durable ledger");
        assertEquals(0, emulator.ledgerSize());
    }

    @Test
    void rollback_unknownCorrelationId_isIdempotent() {
        emulator.rollback("nonexistent");
        assertEquals(0, emulator.ledgerSize());
    }

    @Test
    void read_uncommittedRecord_returnsNull() {
        emulator.prepare("staged-only", "Hello");
        assertNull(emulator.read("staged-only"),
                "Staged-but-not-committed record must not be readable");
    }

    @Test
    void twoPhaseCommit_fullCycle_greetingRoundTrips() {
        String correlationId = "corr-2pc";
        String greeting = "Hello, Enterprise World!";

        byte[] staged = emulator.prepare(correlationId, greeting);
        assertNotNull(staged);

        emulator.commit(correlationId);

        String result = emulator.read(correlationId);
        assertEquals(greeting, result);
    }
}
