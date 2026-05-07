package com.egds.dna;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DnaSequenceEncoder")
class DnaSequenceEncoderTest {

    private DnaSequenceEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new DnaSequenceEncoder();
    }

    @Test
    @DisplayName("encode returns non-null non-empty FASTA string")
    void encodeReturnsNonNullFasta() {
        String result = encoder.encode("seq1", "Hello");
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("FASTA output starts with '>' header line")
    void fastaStartsWithHeader() {
        String result = encoder.encode("seq2", "Hi");
        assertThat(result).startsWith(">");
    }

    @Test
    @DisplayName("FASTA header contains the sequence ID")
    void fastaHeaderContainsSequenceId() {
        String result = encoder.encode("my-seq-42", "test");
        assertThat(result).contains("my-seq-42");
    }

    @Test
    @DisplayName("retrieve returns stored FASTA after encode")
    void retrieveReturnsStoredFasta() {
        encoder.encode("seq3", "World");
        String retrieved = encoder.retrieve("seq3");
        assertThat(retrieved).isNotNull();
    }

    @Test
    @DisplayName("retrieve returns null for unknown sequence ID")
    void retrieveReturnsNullForUnknownId() {
        assertThat(encoder.retrieve("unknown-seq-99"))
                .isNull();
    }

    @Test
    @DisplayName("ASCII 'A' (0x41) encodes to nucleotide CAAC")
    void asciiAEncodesToCaac() {
        String fasta = encoder.encode("single-a", "A");
        String body = fasta
                .substring(fasta.indexOf('\n') + 1)
                .replace("\n", "");
        assertThat(body).isEqualTo("CAAC");
    }

    @Test
    @DisplayName("nucleotide length is 4x the input byte length")
    void nucleotideLengthIsFourTimesInputLength() {
        String input = "Hi!";
        String fasta = encoder.encode("len-check", input);
        String body = fasta
                .substring(fasta.indexOf('\n') + 1)
                .replace("\n", "");
        assertThat(body).hasSize(
                input.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                        .length * 4);
    }
}
