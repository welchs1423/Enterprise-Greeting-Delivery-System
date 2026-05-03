package com.egds.web;

/**
 * Marker record representing the root object returned by the
 * {@code greeting} GraphQL query.
 *
 * <p>This record carries no payload. Each field of the GraphQL
 * {@code GreetingFragment} type is resolved by a dedicated
 * {@code @SchemaMapping} method in {@link GreetingGraphQlController},
 * which simulates a separate virtual microservice (subgraph resolver)
 * fetching its token asynchronously.
 */
public record GreetingFragment() {
}
