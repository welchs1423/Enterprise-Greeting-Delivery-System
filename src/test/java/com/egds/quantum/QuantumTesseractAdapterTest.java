package com.egds.quantum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("QuantumTesseractAdapter")
class QuantumTesseractAdapterTest {

    @Test
    @DisplayName("project completes without exception in JVM mode")
    void projectCompletesWithoutException() {
        QuantumTesseractAdapter adapter =
                new QuantumTesseractAdapter();
        assertThatCode(() ->
                adapter.project("Hello, World!"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("native library is unavailable in JVM test env")
    void nativeLibraryIsNotAvailableInJvm() {
        assertThat(QuantumTesseractAdapter.isNativeAvailable())
                .isFalse();
    }

    @Test
    @DisplayName("project with empty string does not throw")
    void projectWithEmptyStringCompletes() {
        QuantumTesseractAdapter adapter =
                new QuantumTesseractAdapter();
        assertThatCode(() -> adapter.project(""))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("project with long string does not throw")
    void projectWithLongStringCompletes() {
        QuantumTesseractAdapter adapter =
                new QuantumTesseractAdapter();
        String longMessage = "Hello, World! ".repeat(100);
        assertThatCode(() -> adapter.project(longMessage))
                .doesNotThrowAnyException();
    }
}
