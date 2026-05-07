package com.egds.dna;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DNA-sequence encoder with FASTA-format persistence (simulation).
 *
 * <p>Converts arbitrary string data to a nucleotide sequence via
 * a binary-to-base-4 mapping: each 2-bit dibit of the UTF-8
 * byte stream maps to one nucleotide (00=A, 01=C, 10=G, 11=T).
 * Encoded sequences are stored in an in-memory FASTA-formatted
 * record keyed by a caller-supplied sequence identifier.
 */
@Component
public class DnaSequenceEncoder {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(DnaSequenceEncoder.class);

    /** Nucleotide symbols indexed by 2-bit dibit value (0-3). */
    private static final char[] NUCLEOTIDES =
            {'A', 'C', 'G', 'T'};

    /** FASTA sequence line width in nucleotide characters. */
    private static final int FASTA_LINE_WIDTH = 60;

    /** In-memory store keyed by sequence ID. */
    private final ConcurrentMap<String, String> store =
            new ConcurrentHashMap<>();

    /**
     * Encodes the supplied string to a nucleotide sequence and
     * stores it in FASTA format under the given sequence ID.
     *
     * @param sequenceId identifier used for later retrieval
     * @param data       the UTF-8 string to encode
     * @return the FASTA-formatted nucleotide string
     */
    public String encode(
            final String sequenceId, final String data) {
        String nucleotides = toNucleotides(data);
        String fasta = buildFasta(
                sequenceId, nucleotides, data.length());
        store.put(sequenceId, fasta);
        LOG.info(
                "DNA encoded sequenceId={}"
                        + " inputLen={} nuclLen={}",
                sequenceId,
                data.length(),
                nucleotides.length());
        return fasta;
    }

    /**
     * Retrieves a previously stored FASTA sequence by ID.
     *
     * @param sequenceId the identifier used during encoding
     * @return the FASTA string, or {@code null} if absent
     */
    public String retrieve(final String sequenceId) {
        return store.get(sequenceId);
    }

    private static String toNucleotides(final String data) {
        byte[] bytes =
                data.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb =
                new StringBuilder(bytes.length * 4);
        for (byte b : bytes) {
            int unsigned = b & 0xFF;
            for (int shift = 6; shift >= 0; shift -= 2) {
                int dibit = (unsigned >> shift) & 0x3;
                sb.append(NUCLEOTIDES[dibit]);
            }
        }
        return sb.toString();
    }

    private static String buildFasta(
            final String seqId,
            final String nucleotides,
            final int originalLen) {
        StringBuilder sb = new StringBuilder();
        sb.append(">EGDS-DNA|id=")
          .append(seqId)
          .append("|srcLen=")
          .append(originalLen)
          .append("|nuclLen=")
          .append(nucleotides.length())
          .append('\n');
        int len = nucleotides.length();
        for (int i = 0; i < len; i += FASTA_LINE_WIDTH) {
            sb.append(nucleotides, i,
                    Math.min(i + FASTA_LINE_WIDTH, len));
            sb.append('\n');
        }
        return sb.toString();
    }
}
