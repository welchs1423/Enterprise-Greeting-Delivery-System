package com.egds.nano;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;

/**
 * Scatter-gather aggregator that distributes each character of
 * {@code "Hello World"} to a dedicated nano service asynchronously,
 * then reassembles the results in original order.
 *
 * <p>Per-character scatter is performed via
 * {@link CompletableFuture} on the common
 * {@link java.util.concurrent.ForkJoinPool}. Aggregation blocks
 * until all futures complete, preserving positional ordering.
 */
@Component
public class ScatterGatherAggregator {

    /** Nano service mesh holding one service per character. */
    private final LetterNanoServiceMesh mesh;

    /**
     * Constructs the aggregator with its nano service mesh.
     *
     * @param letterMesh the mesh providing per-character services
     */
    public ScatterGatherAggregator(
            final LetterNanoServiceMesh letterMesh) {
        this.mesh = letterMesh;
    }

    /**
     * Scatters each character of the input string to its
     * corresponding nano service asynchronously, then gathers and
     * reassembles the results into the original string.
     *
     * @param input the string to scatter-process; length must equal
     *              the number of registered nano services
     * @return the reassembled string after per-character processing
     * @throws IllegalArgumentException if input length does not match
     *         the number of registered nano services
     * @throws java.util.concurrent.CompletionException if any nano
     *         service future completes exceptionally
     */
    public String scatterGather(final String input) {
        List<LetterNanoService> services = mesh.getServices();
        if (input.length() != services.size()) {
            throw new IllegalArgumentException(
                    "Input length " + input.length()
                    + " does not match service count "
                    + services.size());
        }
        List<CompletableFuture<Character>> futures =
                new ArrayList<>(input.length());
        for (int idx = 0; idx < input.length(); idx++) {
            final char chr = input.charAt(idx);
            final LetterNanoService svc = services.get(idx);
            futures.add(CompletableFuture.supplyAsync(
                    () -> svc.process(chr)));
        }
        StringBuilder assembled = new StringBuilder(input.length());
        for (CompletableFuture<Character> future : futures) {
            assembled.append(future.join());
        }
        return assembled.toString();
    }
}
