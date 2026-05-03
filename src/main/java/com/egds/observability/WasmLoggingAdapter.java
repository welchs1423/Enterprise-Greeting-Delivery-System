package com.egds.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logging adapter that delegates to a Rust-compiled WebAssembly module
 * loaded via JNI.
 *
 * <p>The native shared library ({@code egds_wasm_logger}) is expected to
 * export a function with the JNI mangled name
 * {@code Java_com_egds_observability_WasmLoggingAdapter_wasmLog}, compiled
 * from a Rust crate that embeds a WASM runtime (e.g., Wasmtime or Wasmer).
 *
 * <p>If the library cannot be located at class-loading time (development,
 * CI, or environments without the compiled artifact), the static initialiser
 * records the linkage error and {@link #NATIVE_AVAILABLE} is set to
 * {@code false}. All subsequent calls to {@link #log(String, String)} are
 * transparently rerouted to SLF4J, ensuring the application remains
 * operational without the native artifact.
 *
 * <p>Production deployment instructions: place {@code libegds_wasm_logger.so}
 * (Linux) or {@code egds_wasm_logger.dll} (Windows) on {@code java.library.path}
 * before JVM startup.
 */
@Component
public class WasmLoggingAdapter {

    private static final Logger FALLBACK_LOG =
            LoggerFactory.getLogger(WasmLoggingAdapter.class);

    /**
     * {@code true} when the native WASM logger library was loaded
     * successfully at class initialisation time.
     */
    static final boolean NATIVE_AVAILABLE;

    static {
        boolean loaded;
        try {
            System.loadLibrary("egds_wasm_logger");
            loaded = true;
        } catch (UnsatisfiedLinkError e) {
            FALLBACK_LOG.warn("[WASM] native library egds_wasm_logger"
                    + " unavailable, using SLF4J fallback: {}", e.getMessage());
            loaded = false;
        }
        NATIVE_AVAILABLE = loaded;
    }

    /**
     * JNI entry point implemented in the Rust/WASM native module.
     *
     * <p>Signature corresponds to the JNI mangled name
     * {@code Java_com_egds_observability_WasmLoggingAdapter_wasmLog}.
     * Never called when {@link #NATIVE_AVAILABLE} is {@code false}.
     *
     * @param level   the log-level token (e.g., "INFO", "WARN", "ERROR")
     * @param message the log message
     */
    private native void wasmLog(String level, String message);

    /**
     * Records a log entry via the WASM native module when the library is
     * present, or falls back to SLF4J otherwise.
     *
     * @param level   the log-level token
     * @param message the message to record
     */
    public void log(final String level, final String message) {
        if (NATIVE_AVAILABLE) {
            wasmLog(level, message);
        } else {
            FALLBACK_LOG.info("[WASM-FALLBACK] level={} message={}",
                    level, message);
        }
    }
}
