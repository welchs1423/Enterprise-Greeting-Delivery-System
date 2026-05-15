package com.egds.lootbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.egds.blockchain.GreetingIntegrityVerifier;
import com.egds.core.dto.MessageContentDto;
import com.egds.nepotism.CeosNephewGreetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link GreetingLootboxProvider}.
 */
class GreetingLootboxProviderTest {

    private CeosNephewGreetingService ceosNephewService;
    private GreetingIntegrityVerifier integrityVerifier;

    /** Sets up mocks before each test. */
    @BeforeEach
    void setUp() {
        ceosNephewService = mock(CeosNephewGreetingService.class);
        integrityVerifier = mock(GreetingIntegrityVerifier.class);
    }

    /** Verifies roll 0 (common tier) returns "Hi". */
    @Test
    void commonDropReturnsHi() {
        GreetingLootboxProvider provider = enabledProvider(0);
        MessageContentDto result = provider.provideMessage();
        assertThat(result.getContent()).isEqualTo("Hi");
    }

    /** Verifies roll 80 (rare tier) returns "Hello". */
    @Test
    void rareDropReturnsHello() {
        GreetingLootboxProvider provider = enabledProvider(80);
        MessageContentDto result = provider.provideMessage();
        assertThat(result.getContent()).isEqualTo("Hello");
    }

    /** Verifies roll 98 (rare tier upper bound) returns "Hello". */
    @Test
    void rareDropUpperBoundReturnsHello() {
        GreetingLootboxProvider provider = enabledProvider(98);
        MessageContentDto result = provider.provideMessage();
        assertThat(result.getContent()).isEqualTo("Hello");
    }

    /** Verifies roll 99 (jackpot) delegates to CeosNephewGreetingService. */
    @Test
    void jackpotDelegatesToCeosNephewService() {
        MessageContentDto jackpot =
                new MessageContentDto.Builder("Hello, World!", "c1")
                        .build();
        when(ceosNephewService.provideMessage()).thenReturn(jackpot);
        GreetingLootboxProvider provider = enabledProvider(99);
        MessageContentDto result = provider.provideMessage();
        verify(ceosNephewService).provideMessage();
        assertThat(result.getContent()).isEqualTo("Hello, World!");
    }

    /** Verifies disabled lootbox always delegates unconditionally. */
    @Test
    void disabledLootboxAlwaysDelegates() {
        MessageContentDto expected =
                new MessageContentDto.Builder("Hello, World!", "c2")
                        .build();
        when(ceosNephewService.provideMessage()).thenReturn(expected);
        // Default Java boolean is false → lootboxEnabled = false
        GreetingLootboxProvider provider =
                new GreetingLootboxProvider(
                        ceosNephewService, integrityVerifier);
        MessageContentDto result = provider.provideMessage();
        verify(ceosNephewService).provideMessage();
        assertThat(result).isSameAs(expected);
    }

    /** Verifies drop results populate a non-blank correlationId. */
    @Test
    void dropResultsHaveNonNullCorrelationId() {
        GreetingLootboxProvider provider = enabledProvider(0);
        MessageContentDto result = provider.provideMessage();
        assertThat(result.getCorrelationId()).isNotBlank();
    }

    /** Verifies common drops register the hash with integrity verifier. */
    @Test
    void commonDropRegistersIntegrityHash() {
        GreetingLootboxProvider provider = enabledProvider(0);
        provider.provideMessage();
        verify(integrityVerifier).register(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.contains("Hi"));
    }

    private GreetingLootboxProvider enabledProvider(final int fixedRoll) {
        GreetingLootboxProvider p = new GreetingLootboxProvider(
                ceosNephewService, integrityVerifier) {
            @Override
            int rollDice() {
                return fixedRoll;
            }
        };
        ReflectionTestUtils.setField(p, "lootboxEnabled", true);
        return p;
    }
}
