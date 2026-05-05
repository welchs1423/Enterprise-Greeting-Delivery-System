package com.egds.mainframe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emulates an IBM AS/400 (IBM i) mainframe ledger for greeting persistence.
 *
 * <p>All data is stored internally as EBCDIC (IBM Code Page 037) bytes to
 * satisfy the dual-ledger requirement that the mainframe record matches the
 * character encoding used by physical AS/400 DB2 for i table columns.
 *
 * <p>Writes are coordinated via a mock two-phase commit (2PC) protocol so
 * that the mainframe ledger and the blockchain ledger remain consistent:
 * <ol>
 *   <li>Phase 1 — {@link #prepare}: the EBCDIC record is written to a
 *       volatile staging buffer; no durable write occurs.</li>
 *   <li>Phase 2a — {@link #commit}: the staged record is promoted to the
 *       durable in-memory ledger.</li>
 *   <li>Phase 2b — {@link #rollback}: the staged record is discarded.</li>
 * </ol>
 *
 * <p>No external IBM i connection is made; this is a fully in-memory
 * simulation intended for development and testing environments.
 */
@Service
public class As400MainframeEmulator {

    /** Logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(As400MainframeEmulator.class);

    /**
     * EBCDIC Code Page 037 — US/Canada variant used in IBM AS/400 systems.
     * Canonical Java charset name: IBM037.
     */
    private static final Charset EBCDIC = Charset.forName("IBM037");

    /** Durable ledger: correlationId to EBCDIC-encoded greeting bytes. */
    private final ConcurrentHashMap<String, byte[]> ledger =
            new ConcurrentHashMap<>();

    /** 2PC staging buffer for in-flight prepare operations. */
    private final ConcurrentHashMap<String, byte[]> stagingBuffer =
            new ConcurrentHashMap<>();

    /**
     * Phase 1 of the two-phase commit protocol.
     *
     * <p>Converts {@code greeting} to EBCDIC (IBM037) and writes the
     * encoded bytes to the volatile staging buffer. The durable ledger
     * is not updated until {@link #commit} is called.
     *
     * @param correlationId unique request identifier used as the ledger key
     * @param greeting      the UTF-8 greeting string to encode and stage
     * @return a defensive copy of the EBCDIC bytes placed in the staging
     *         buffer, for inspection by the 2PC coordinator
     */
    public byte[] prepare(final String correlationId,
                          final String greeting) {
        byte[] ebcdic = greeting.getBytes(EBCDIC);
        stagingBuffer.put(correlationId, ebcdic);
        LOG.info("[AS400] 2PC prepare correlationId={} ebcdic_len={}",
                correlationId, ebcdic.length);
        return Arrays.copyOf(ebcdic, ebcdic.length);
    }

    /**
     * Phase 2a of the two-phase commit protocol.
     *
     * <p>Promotes the record staged by {@link #prepare} to the durable
     * ledger and removes it from the staging buffer.
     *
     * @param correlationId the correlationId previously staged via
     *                      {@link #prepare}
     * @throws IllegalStateException if no staged record exists for the
     *                               supplied correlationId
     */
    public void commit(final String correlationId) {
        byte[] staged = stagingBuffer.remove(correlationId);
        if (staged == null) {
            throw new IllegalStateException(
                    "AS400 2PC commit failed: no staged record for "
                            + correlationId);
        }
        ledger.put(correlationId, staged);
        LOG.info("[AS400] 2PC commit correlationId={}", correlationId);
    }

    /**
     * Phase 2b of the two-phase commit protocol.
     *
     * <p>Discards the staged record without promoting it to the durable
     * ledger. Idempotent: calling rollback for an unknown correlationId
     * is a no-op.
     *
     * @param correlationId the correlationId to roll back
     */
    public void rollback(final String correlationId) {
        stagingBuffer.remove(correlationId);
        LOG.warn("[AS400] 2PC rollback correlationId={}", correlationId);
    }

    /**
     * Reads a committed ledger entry and decodes it from EBCDIC to a
     * Java {@code String} (UTF-16 internally).
     *
     * @param correlationId the correlationId to look up
     * @return the decoded greeting string, or {@code null} if not found
     */
    public String read(final String correlationId) {
        byte[] ebcdic = ledger.get(correlationId);
        if (ebcdic == null) {
            return null;
        }
        return new String(ebcdic, EBCDIC);
    }

    /**
     * Returns the number of committed records in the durable ledger.
     *
     * @return ledger entry count
     */
    public int ledgerSize() {
        return ledger.size();
    }
}
