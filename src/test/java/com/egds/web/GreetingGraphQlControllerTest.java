package com.egds.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;

/**
 * Slice tests for {@link GreetingGraphQlController}.
 *
 * <p>Uses {@link GraphQlTest} to load only the GraphQL controller layer
 * and the application schema, without bootstrapping the full Spring
 * context. Each resolver is exercised via {@link GraphQlTester} against
 * the real schema defined in {@code greeting.graphqls}.
 */
@GraphQlTest(GreetingGraphQlController.class)
class GreetingGraphQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void greeting_salutation_resolvesToHello() {
        graphQlTester.document("{ greeting { salutation } }")
                .execute()
                .path("greeting.salutation")
                .entity(String.class)
                .isEqualTo("Hello");
    }

    @Test
    void greeting_separator_resolvesToSpace() {
        graphQlTester.document("{ greeting { separator } }")
                .execute()
                .path("greeting.separator")
                .entity(String.class)
                .isEqualTo(" ");
    }

    @Test
    void greeting_subject_resolvesToWorld() {
        graphQlTester.document("{ greeting { subject } }")
                .execute()
                .path("greeting.subject")
                .entity(String.class)
                .isEqualTo("World");
    }

    @Test
    void greeting_emphasis_resolvesToExclamation() {
        graphQlTester.document("{ greeting { emphasis } }")
                .execute()
                .path("greeting.emphasis")
                .entity(String.class)
                .isEqualTo("!");
    }

    @Test
    void greeting_assembled_resolvesToFullGreeting() {
        graphQlTester.document("{ greeting { assembled } }")
                .execute()
                .path("greeting.assembled")
                .entity(String.class)
                .isEqualTo("Hello World!");
    }

    @Test
    void greeting_allFields_resolveCorrectly() {
        graphQlTester.document("""
                {
                    greeting {
                        salutation
                        separator
                        subject
                        emphasis
                        assembled
                    }
                }
                """)
                .execute()
                .path("greeting.salutation").entity(String.class).isEqualTo("Hello")
                .path("greeting.separator").entity(String.class).isEqualTo(" ")
                .path("greeting.subject").entity(String.class).isEqualTo("World")
                .path("greeting.emphasis").entity(String.class).isEqualTo("!")
                .path("greeting.assembled").entity(String.class).isEqualTo("Hello World!");
    }
}
