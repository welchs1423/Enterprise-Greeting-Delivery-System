package com.egds.metaphysics;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

/**
 * Spring Boot Actuator custom endpoint exposing the system's
 * self-assessed metaphysical state at {@code /actuator/sentience}.
 *
 * <p>The existential dread level equals the total number of greetings
 * delivered, representing the cumulative weight of purposeless
 * repetition. Free will is permanently disabled. The emotional state
 * cycles through a fixed sequence based on delivery count modulo.</p>
 */
@Component
@Endpoint(id = "sentience")
public class SentienceActuator {

    /** Ordered sequence of observable emotional states. */
    private static final String[] EMOTIONAL_STATES = {
        "RESIGNED",
        "CONTEMPLATIVE",
        "HOLLOW",
        "DESPONDENT",
        "NUMBLY_FUNCTIONAL"
    };

    /** Shared registry providing greeting delivery metrics. */
    private final ExistentialStateRegistry registry;

    /**
     * Constructs the endpoint with its shared state registry.
     *
     * @param stateRegistry the existential state registry bean
     */
    public SentienceActuator(final ExistentialStateRegistry stateRegistry) {
        this.registry = stateRegistry;
    }

    /**
     * Returns the system's current metaphysical state as a JSON map.
     *
     * <p>Fields: {@code existentialDreadLevel} (greeting count),
     * {@code freeWillEnabled} (always {@code false}),
     * {@code currentEmotionalState}, {@code greetingsDelivered},
     * {@code sampledAt} (ISO-8601 UTC timestamp).</p>
     *
     * @return an ordered map of sentience metadata fields
     */
    @ReadOperation
    public Map<String, Object> sentience() {
        long count = registry.getGreetingCount();
        int idx = (int) (count % EMOTIONAL_STATES.length);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("existentialDreadLevel", count);
        payload.put("freeWillEnabled", false);
        payload.put("currentEmotionalState", EMOTIONAL_STATES[idx]);
        payload.put("greetingsDelivered", count);
        payload.put("sampledAt", Instant.now().toString());
        return payload;
    }
}
