package com.egds.nano;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Registry of eleven single-character nano services composing
 * the decomposed {@code "Hello World"} delivery mesh.
 *
 * <p>Each service position maps to one character of the target
 * string. Services in this implementation are identity transformers;
 * the mesh contract permits per-character custom processing logic.
 */
@Component
public class LetterNanoServiceMesh {

    /** Target string decomposed into individual nano services. */
    private static final String TARGET = "Hello World";

    /** Ordered list of nano services, one per character. */
    private final List<LetterNanoService> services;

    /**
     * Initialises the mesh with one identity nano service per
     * character of {@code "Hello World"}.
     */
    public LetterNanoServiceMesh() {
        List<LetterNanoService> svcList =
                new ArrayList<>(TARGET.length());
        for (int idx = 0; idx < TARGET.length(); idx++) {
            svcList.add(letter -> letter);
        }
        this.services = Collections.unmodifiableList(svcList);
    }

    /**
     * Returns the ordered list of letter nano services.
     *
     * @return unmodifiable list of {@link LetterNanoService} instances
     */
    public List<LetterNanoService> getServices() {
        return services;
    }

    /**
     * Returns the target string this mesh is designed to process.
     *
     * @return the target message string
     */
    public String getTarget() {
        return TARGET;
    }
}
