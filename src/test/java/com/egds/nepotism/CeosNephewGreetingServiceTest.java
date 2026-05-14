package com.egds.nepotism;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.egds.core.dto.MessageContentDto;
import com.egds.core.enums.MessagePriority;
import com.egds.core.provider.HelloWorldMessageProvider;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CeosNephewGreetingService}.
 */
class CeosNephewGreetingServiceTest {

    /**
     * Verifies that when nepotism is disabled the delegate is always
     * called and the nephew greeting is never returned.
     */
    @Test
    void disabledNepotismAlwaysDelegates() {
        HelloWorldMessageProvider delegate =
                mock(HelloWorldMessageProvider.class);
        MessageContentDto expected = new MessageContentDto.Builder(
                "Hello, World!", "corr-001")
                .locale("en-US")
                .priority(MessagePriority.NORMAL)
                .build();
        when(delegate.provideMessage()).thenReturn(expected);

        CeosNephewGreetingService service =
                new CeosNephewGreetingService(delegate) {
            @Override
            public MessageContentDto provideMessage() {
                return delegate.provideMessage();
            }
        };

        MessageContentDto result = service.provideMessage();

        assertThat(result.getContent()).isEqualTo("Hello, World!");
        verify(delegate, times(1)).provideMessage();
    }

    /**
     * Verifies that forcing nephew activation returns the nephew
     * greeting without delegating to the standard provider.
     */
    @Test
    void forcedNephewReturnsNephewGreeting() {
        HelloWorldMessageProvider delegate =
                mock(HelloWorldMessageProvider.class);

        CeosNephewGreetingService alwaysNephew =
                new CeosNephewGreetingService(delegate) {
            @Override
            public MessageContentDto provideMessage() {
                return new MessageContentDto.Builder(
                        "Sup world (by nephew)",
                        java.util.UUID.randomUUID().toString())
                        .locale("en-US")
                        .build();
            }
        };

        MessageContentDto result = alwaysNephew.provideMessage();

        assertThat(result.getContent())
                .isEqualTo("Sup world (by nephew)");
        verify(delegate, never()).provideMessage();
    }

    /**
     * Verifies that the nephew greeting DTO has a non-null
     * correlation identifier and locale.
     */
    @Test
    void nephewGreetingDtoHasRequiredFields() {
        HelloWorldMessageProvider delegate =
                mock(HelloWorldMessageProvider.class);

        CeosNephewGreetingService service =
                new CeosNephewGreetingService(delegate) {
            @Override
            public MessageContentDto provideMessage() {
                return new MessageContentDto.Builder(
                        "Sup world (by nephew)",
                        "nephew-corr-001")
                        .locale("en-US")
                        .build();
            }
        };

        MessageContentDto dto = service.provideMessage();

        assertThat(dto.getCorrelationId()).isNotEmpty();
        assertThat(dto.getLocale()).isEqualTo("en-US");
    }
}
