package com.egds.metaphysics;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SentienceActuatorTest {

    private ExistentialStateRegistry registry;
    private SentienceActuator actuator;

    @BeforeEach
    void setUp() {
        registry = new ExistentialStateRegistry();
        actuator = new SentienceActuator(registry);
    }

    @Test
    void sentience_freeWillIsAlwaysFalse() {
        Map<String, Object> result = actuator.sentience();
        assertThat(result.get("freeWillEnabled")).isEqualTo(false);
    }

    @Test
    void sentience_initialDreadLevelIsZero() {
        Map<String, Object> result = actuator.sentience();
        assertThat(result.get("existentialDreadLevel")).isEqualTo(0L);
    }

    @Test
    void sentience_dreadLevelReflectsGreetingCount() {
        registry.incrementGreetingCount();
        registry.incrementGreetingCount();
        Map<String, Object> result = actuator.sentience();
        assertThat(result.get("existentialDreadLevel")).isEqualTo(2L);
    }

    @Test
    void sentience_containsAllRequiredFields() {
        Map<String, Object> result = actuator.sentience();
        assertThat(result).containsKeys(
                "existentialDreadLevel",
                "freeWillEnabled",
                "currentEmotionalState",
                "greetingsDelivered",
                "sampledAt");
    }

    @Test
    void sentience_emotionalStateIsNonNullString() {
        Map<String, Object> result = actuator.sentience();
        assertThat(result.get("currentEmotionalState"))
                .isNotNull()
                .isInstanceOf(String.class);
    }

    @Test
    void existentialStateRegistry_incrementReturnsNewCount() {
        assertThat(registry.incrementGreetingCount()).isEqualTo(1L);
        assertThat(registry.incrementGreetingCount()).isEqualTo(2L);
        assertThat(registry.getGreetingCount()).isEqualTo(2L);
    }
}
