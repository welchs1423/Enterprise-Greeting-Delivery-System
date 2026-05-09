package com.egds.core.aspect;

import com.egds.metaphysics.ExistentialStateRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExistentialLoggingAspectTest {

    private ExistentialStateRegistry registry;
    private ExistentialLoggingAspect aspect;

    @BeforeEach
    void setUp() {
        registry = new ExistentialStateRegistry();
        aspect = new ExistentialLoggingAspect(registry);
    }

    @Test
    void aroundGreetingOutput_proceedsWithJoinPoint() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn(null);
        aspect.aroundGreetingOutput(pjp);
        verify(pjp).proceed();
    }

    @Test
    void aroundGreetingOutput_incrementsGreetingCount()
            throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn(null);
        aspect.aroundGreetingOutput(pjp);
        assertThat(registry.getGreetingCount()).isEqualTo(1L);
    }

    @Test
    void aroundGreetingOutput_multipleCallsAccumulateCount()
            throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn(null);
        aspect.aroundGreetingOutput(pjp);
        aspect.aroundGreetingOutput(pjp);
        aspect.aroundGreetingOutput(pjp);
        assertThat(registry.getGreetingCount()).isEqualTo(3L);
    }

    @Test
    void aroundGreetingOutput_returnsJoinPointResult()
            throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn("delivered");
        Object result = aspect.aroundGreetingOutput(pjp);
        assertThat(result).isEqualTo("delivered");
    }

    @Test
    void aroundGreetingOutput_propagatesJoinPointException()
            throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenThrow(
                new RuntimeException("pipeline error"));
        assertThatThrownBy(() -> aspect.aroundGreetingOutput(pjp))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("pipeline error");
    }
}
