package com.egds.mycelial;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class MycelialNetworkAdapterTest {

    private MycelialNetworkAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MycelialNetworkAdapter();
    }

    @Test
    void broadcast_asciiText_returnsByteCountFrequencies() {
        List<Double> freqs = adapter.broadcast("corr-1", "Hi");
        assertThat(freqs).hasSize(2);
    }

    @Test
    void encodeToFrequencies_zeroByteValue_returnsBaseFrequency() {
        byte[] bytes = {0};
        List<Double> freqs = adapter.encodeToFrequencies(bytes);
        assertThat(freqs.get(0))
                .isCloseTo(MycelialNetworkAdapter.BASE_FREQ_HZ, within(1e-10));
    }

    @Test
    void encodeToFrequencies_maxByteValue_returnsUpperBoundFrequency() {
        byte[] bytes = {(byte) 255};
        List<Double> freqs = adapter.encodeToFrequencies(bytes);
        double expectedMax = MycelialNetworkAdapter.BASE_FREQ_HZ
                + MycelialNetworkAdapter.FREQ_RANGE_HZ;
        assertThat(freqs.get(0)).isCloseTo(expectedMax, within(1e-9));
    }

    @Test
    void encodeToFrequencies_allResultsWithinRange() {
        List<Double> freqs = adapter.broadcast("corr-2", "Hello, World!");
        double minFreq = MycelialNetworkAdapter.BASE_FREQ_HZ;
        double maxFreq = MycelialNetworkAdapter.BASE_FREQ_HZ
                + MycelialNetworkAdapter.FREQ_RANGE_HZ;
        for (Double freq : freqs) {
            assertThat(freq).isBetween(minFreq, maxFreq);
        }
    }

    @Test
    void encodeToFrequencies_emptyBytes_returnsEmptyList() {
        List<Double> freqs = adapter.encodeToFrequencies(new byte[0]);
        assertThat(freqs).isEmpty();
    }
}
