package com.egds.nano;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScatterGatherAggregatorTest {

    private ScatterGatherAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new ScatterGatherAggregator(
                new LetterNanoServiceMesh());
    }

    @Test
    void scatterGather_reassemblesHelloWorld() {
        String result = aggregator.scatterGather("Hello World");
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    void scatterGather_preservesCharacterOrder() {
        String result = aggregator.scatterGather("Hello World");
        assertThat(result).hasSize(11);
        assertThat(result.charAt(0)).isEqualTo('H');
        assertThat(result.charAt(5)).isEqualTo(' ');
        assertThat(result.charAt(6)).isEqualTo('W');
    }

    @Test
    void scatterGather_throwsOnLengthMismatch() {
        assertThatThrownBy(() -> aggregator.scatterGather("Hi"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match");
    }
}
