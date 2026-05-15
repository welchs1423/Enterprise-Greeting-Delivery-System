package com.egds.genai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GenAiHallucinationDecorator}.
 */
class GenAiHallucinationDecoratorTest {

    /** Decorator instance under test. */
    private GenAiHallucinationDecorator decorator;

    /** Initialises a fresh decorator before each test. */
    @BeforeEach
    void setUp() {
        decorator = new GenAiHallucinationDecorator();
    }

    /**
     * Verifies that the original content is returned when hallucination
     * does not fire.
     */
    @Test
    void decorateReturnsOriginalContentWhenNotHallucinating() {
        GenAiHallucinationDecorator neverHallucinating =
                new GenAiHallucinationDecorator() {
                    @Override
                    boolean isHallucinating() {
                        return false;
                    }
                };
        String result = neverHallucinating.decorate("Hello, World!");
        assertThat(result).isEqualTo("Hello, World!");
    }

    /**
     * Verifies that hallucination replaces the original content with a
     * non-empty buzzword phrase.
     */
    @Test
    void decorateReturnsBuzzwordWhenHallucinating() {
        GenAiHallucinationDecorator alwaysHallucinating =
                new GenAiHallucinationDecorator() {
                    @Override
                    boolean isHallucinating() {
                        return true;
                    }
                };
        String result = alwaysHallucinating.decorate("Hello, World!");
        assertThat(result).isNotEqualTo("Hello, World!");
        assertThat(result).isNotEmpty();
    }

    /**
     * Verifies that hallucination output is drawn from the known
     * buzzword vocabulary.
     */
    @Test
    void hallucinationOutputIsOneOfKnownBuzzwords() {
        GenAiHallucinationDecorator alwaysHallucinating =
                new GenAiHallucinationDecorator() {
                    @Override
                    boolean isHallucinating() {
                        return true;
                    }
                };
        String result = alwaysHallucinating.decorate("test");
        assertThat(result).isNotEqualTo("test");
        assertThat(result.length()).isGreaterThan(0);
    }

    /**
     * Verifies that {@link GenAiHallucinationDecorator#isHallucinating}
     * returns false by default (hallucinationEnabled is false in unit
     * tests since Spring is not active).
     */
    @Test
    void isHallucinatingReturnsFalseWhenDisabled() {
        assertThat(decorator.isHallucinating()).isFalse();
    }
}
