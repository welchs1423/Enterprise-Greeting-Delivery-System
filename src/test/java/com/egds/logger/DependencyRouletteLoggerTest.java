package com.egds.logger;

import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DependencyRouletteLoggerTest {

    @Test
    void log_julBackend_doesNotThrow() {
        Random rng = mock(Random.class);
        when(rng.nextInt(anyInt())).thenReturn(0);
        DependencyRouletteLogger logger =
                new DependencyRouletteLogger(rng);
        assertDoesNotThrow(() -> logger.log("test"));
    }

    @Test
    void log_stdoutBackend_doesNotThrow() {
        Random rng = mock(Random.class);
        when(rng.nextInt(anyInt())).thenReturn(1);
        DependencyRouletteLogger logger =
                new DependencyRouletteLogger(rng);
        assertDoesNotThrow(() -> logger.log("test"));
    }

    @Test
    void log_stderrBackend_doesNotThrow() {
        Random rng = mock(Random.class);
        when(rng.nextInt(anyInt())).thenReturn(2);
        DependencyRouletteLogger logger =
                new DependencyRouletteLogger(rng);
        assertDoesNotThrow(() -> logger.log("test"));
    }

    @Test
    void log_slf4jBackend_doesNotThrow() {
        Random rng = mock(Random.class);
        when(rng.nextInt(anyInt())).thenReturn(3);
        DependencyRouletteLogger logger =
                new DependencyRouletteLogger(rng);
        assertDoesNotThrow(() -> logger.log("test"));
    }
}
