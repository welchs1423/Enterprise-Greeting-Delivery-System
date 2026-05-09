package com.egds.multiverse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class MultiverseConsistencyManagerTest {

    private MultiverseConsistencyManager manager;

    @BeforeEach
    void setUp() {
        manager = new MultiverseConsistencyManager();
    }

    @Test
    void verifyConsistency_deterministicGreeting_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                manager.verifyConsistency("corr-1", "Hello, World!"));
    }

    @Test
    void verifyConsistency_emptyString_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                manager.verifyConsistency("corr-2", ""));
    }

    @Test
    void verifyConsistency_unicodeGreeting_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                manager.verifyConsistency("corr-3", "Hola, Mundo!"));
    }

    @Test
    void greetingHashService_sameInput_producesSameHash() {
        String h1 = GreetingHashService.computeHash("Hello");
        String h2 = GreetingHashService.computeHash("Hello");
        assertThat(h1).isEqualTo(h2);
    }

    @Test
    void greetingHashService_differentInputs_produceDifferentHashes() {
        String h1 = GreetingHashService.computeHash("Hello");
        String h2 = GreetingHashService.computeHash("World");
        assertThat(h1).isNotEqualTo(h2);
    }

    @Test
    void greetingHashService_resultIsHexString() {
        String hash = GreetingHashService.computeHash("test");
        assertThat(hash).matches("[0-9a-f]{64}");
    }

    @Test
    void dimensionalRiftException_preservesMessage() {
        String msg = "rift in sector 7G";
        DimensionalRiftException ex = new DimensionalRiftException(msg);
        assertThat(ex.getMessage()).isEqualTo(msg);
    }
}
