package com.egds.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.egds.core.exception.PaperJamException;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DotMatrixPrinterAdapter}.
 */
class DotMatrixPrinterAdapterTest {

    /** Verifies that printing proceeds normally when no jam is triggered. */
    @Test
    void printSucceedsWhenNoJam() {
        Random noJam = mock(Random.class);
        when(noJam.nextDouble()).thenReturn(0.99);
        DotMatrixPrinterAdapter printer =
                new DotMatrixPrinterAdapter(noJam, 0L);
        assertThatNoException().isThrownBy(() -> printer.print("Hello"));
    }

    /** Verifies that PaperJamException is thrown when jam is triggered. */
    @Test
    void printThrowsPaperJamExceptionOnJam() {
        Random jammer = mock(Random.class);
        when(jammer.nextDouble()).thenReturn(0.01);
        when(jammer.nextInt(anyInt())).thenReturn(2);
        DotMatrixPrinterAdapter printer =
                new DotMatrixPrinterAdapter(jammer, 0L);

        assertThatThrownBy(() -> printer.print("Hello"))
                .isInstanceOf(PaperJamException.class);
    }

    /** Verifies that the jam exception carries the correct character index. */
    @Test
    void paperJamExceptionReportsCorrectIndex() {
        Random jammer = mock(Random.class);
        when(jammer.nextDouble()).thenReturn(0.01);
        when(jammer.nextInt(anyInt())).thenReturn(3);
        DotMatrixPrinterAdapter printer =
                new DotMatrixPrinterAdapter(jammer, 0L);

        PaperJamException ex = catchThrowableOfType(
                () -> printer.print("Hello!"), PaperJamException.class);

        assertThat(ex).isNotNull();
        assertThat(ex.getCharacterIndex()).isEqualTo(3);
    }

    /** Verifies that an empty string never triggers a paper jam. */
    @Test
    void printEmptyStringNeverJams() {
        Random alwaysJam = mock(Random.class);
        when(alwaysJam.nextDouble()).thenReturn(0.01);
        DotMatrixPrinterAdapter printer =
                new DotMatrixPrinterAdapter(alwaysJam, 0L);
        assertThatNoException().isThrownBy(() -> printer.print(""));
    }
}
