package com.egds.jira;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JiraDrivenExecutionInterceptorTest {

    private JiraDrivenExecutionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JiraDrivenExecutionInterceptor();
    }

    @Test
    void aroundPipelineExecute_proceedsWithJoinPoint()
            throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature sig = mock(Signature.class);
        when(pjp.getSignature()).thenReturn(sig);
        when(sig.getName()).thenReturn("execute");
        when(pjp.proceed()).thenReturn(null);
        interceptor.aroundPipelineExecute(pjp);
        verify(pjp).proceed();
    }

    @Test
    void aroundPipelineExecute_returnsJoinPointResult()
            throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature sig = mock(Signature.class);
        when(pjp.getSignature()).thenReturn(sig);
        when(sig.getName()).thenReturn("execute");
        when(pjp.proceed()).thenReturn("result");
        Object result = interceptor.aroundPipelineExecute(pjp);
        assertThat(result).isEqualTo("result");
    }

    @Test
    void aroundPipelineExecute_propagatesException()
            throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature sig = mock(Signature.class);
        when(pjp.getSignature()).thenReturn(sig);
        when(sig.getName()).thenReturn("execute");
        when(pjp.proceed()).thenThrow(
                new RuntimeException("pipeline error"));
        assertThatThrownBy(
                () -> interceptor.aroundPipelineExecute(pjp))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("pipeline error");
    }
}
