package com.egds.observability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link WasmLoggingAdapter}.
 *
 * <p>The native library {@code egds_wasm_logger} is not present in the
 * test classpath, so {@link WasmLoggingAdapter#NATIVE_AVAILABLE} must be
 * {@code false} and all log calls must route through the SLF4J fallback
 * without throwing.
 */
class WasmLoggingAdapterTest {

    private WasmLoggingAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WasmLoggingAdapter();
    }

    @Test
    void nativeLibrary_isNotAvailableInTestEnvironment() {
        assertFalse(WasmLoggingAdapter.NATIVE_AVAILABLE,
                "Native egds_wasm_logger must not be present in the test"
                        + " classpath");
    }

    @Test
    void log_infoLevel_doesNotThrowInFallbackMode() {
        assertDoesNotThrow(
                () -> adapter.log("INFO", "test message via fallback"));
    }

    @Test
    void log_warnLevel_doesNotThrowInFallbackMode() {
        assertDoesNotThrow(
                () -> adapter.log("WARN", "warn test via fallback"));
    }

    @Test
    void log_errorLevel_doesNotThrowInFallbackMode() {
        assertDoesNotThrow(
                () -> adapter.log("ERROR", "error test via fallback"));
    }

    @Test
    void log_emptyMessage_doesNotThrow() {
        assertDoesNotThrow(() -> adapter.log("INFO", ""));
    }
}
