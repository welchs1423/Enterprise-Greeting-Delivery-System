package com.egds.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * GraphQL controller implementing the EGDS Supergraph query gateway.
 *
 * <p>Each field of the {@code GreetingFragment} GraphQL type is resolved
 * by a dedicated {@code @SchemaMapping} method that executes on a virtual
 * thread (via a cached thread pool), simulating independent subgraph
 * resolvers owned by separate downstream microservices:
 *
 * <ul>
 *   <li><b>salutation</b>  — Salutation Subgraph: returns {@code "Hello"}</li>
 *   <li><b>separator</b>   — Formatting Subgraph: returns {@code " "}</li>
 *   <li><b>subject</b>     — Subject Subgraph:    returns {@code "World"}</li>
 *   <li><b>emphasis</b>    — Emphasis Subgraph:   returns {@code "!"}</li>
 *   <li><b>assembled</b>   — Gateway assembler:   concatenates all tokens</li>
 * </ul>
 *
 * <p>All field resolvers run asynchronously by returning
 * {@link CompletableFuture}. Spring for GraphQL dispatches them
 * concurrently and merges the results before serialising the response.
 *
 * <p>Access: this endpoint is intentionally unauthenticated to provide
 * a separate entry point from the JWT-secured REST path. Restrict it
 * via Spring Security if a production deployment requires authorisation.
 */
@Controller
public class GreetingGraphQlController {

    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingGraphQlController.class);

    /**
     * Shared executor for async subgraph resolution.
     * Uses the common {@link ForkJoinPool} which provides work-stealing
     * parallelism suitable for the short-lived, CPU-light resolver tasks.
     */
    private static final Executor SUBGRAPH_EXECUTOR =
            ForkJoinPool.commonPool();

    /**
     * Entry-point resolver for the {@code greeting} root query.
     * Returns a marker {@link GreetingFragment}; each field is resolved
     * independently by the {@code @SchemaMapping} methods below.
     *
     * @return an empty {@link GreetingFragment} marker object
     */
    @QueryMapping
    public GreetingFragment greeting() {
        LOG.debug("[GRAPHQL] greeting query received, dispatching subgraph"
                + " resolvers");
        return new GreetingFragment();
    }

    /**
     * Salutation Subgraph resolver.
     * Simulates a remote call to the salutation microservice.
     *
     * @param fragment the parent {@link GreetingFragment} (unused)
     * @return a {@link CompletableFuture} resolving to {@code "Hello"}
     */
    @SchemaMapping(typeName = "GreetingFragment", field = "salutation")
    public CompletableFuture<String> salutation(
            final GreetingFragment fragment) {
        return CompletableFuture.supplyAsync(() -> {
            LOG.debug("[GRAPHQL][SUBGRAPH:SALUTATION] resolving token");
            return "Hello";
        }, SUBGRAPH_EXECUTOR);
    }

    /**
     * Formatting Subgraph resolver.
     * Simulates a remote call to the whitespace/formatting microservice.
     *
     * @param fragment the parent {@link GreetingFragment} (unused)
     * @return a {@link CompletableFuture} resolving to {@code " "}
     */
    @SchemaMapping(typeName = "GreetingFragment", field = "separator")
    public CompletableFuture<String> separator(
            final GreetingFragment fragment) {
        return CompletableFuture.supplyAsync(() -> {
            LOG.debug("[GRAPHQL][SUBGRAPH:SEPARATOR] resolving token");
            return " ";
        }, SUBGRAPH_EXECUTOR);
    }

    /**
     * Subject Subgraph resolver.
     * Simulates a remote call to the subject-entity microservice.
     *
     * @param fragment the parent {@link GreetingFragment} (unused)
     * @return a {@link CompletableFuture} resolving to {@code "World"}
     */
    @SchemaMapping(typeName = "GreetingFragment", field = "subject")
    public CompletableFuture<String> subject(
            final GreetingFragment fragment) {
        return CompletableFuture.supplyAsync(() -> {
            LOG.debug("[GRAPHQL][SUBGRAPH:SUBJECT] resolving token");
            return "World";
        }, SUBGRAPH_EXECUTOR);
    }

    /**
     * Emphasis Subgraph resolver.
     * Simulates a remote call to the punctuation/emphasis microservice.
     *
     * @param fragment the parent {@link GreetingFragment} (unused)
     * @return a {@link CompletableFuture} resolving to {@code "!"}
     */
    @SchemaMapping(typeName = "GreetingFragment", field = "emphasis")
    public CompletableFuture<String> emphasis(
            final GreetingFragment fragment) {
        return CompletableFuture.supplyAsync(() -> {
            LOG.debug("[GRAPHQL][SUBGRAPH:EMPHASIS] resolving token");
            return "!";
        }, SUBGRAPH_EXECUTOR);
    }

    /**
     * Gateway assembler resolver.
     * Concatenates all tokens into the final greeting string.
     * In a true federated supergraph this would fan-out to all subgraphs
     * and merge; here it derives the result locally for demonstration.
     *
     * @param fragment the parent {@link GreetingFragment} (unused)
     * @return a {@link CompletableFuture} resolving to {@code "Hello World!"}
     */
    @SchemaMapping(typeName = "GreetingFragment", field = "assembled")
    public CompletableFuture<String> assembled(
            final GreetingFragment fragment) {
        return CompletableFuture.supplyAsync(() -> {
            LOG.info("[GRAPHQL][GATEWAY] assembling greeting from subgraph"
                    + " tokens");
            return "Hello World!";
        }, SUBGRAPH_EXECUTOR);
    }
}
