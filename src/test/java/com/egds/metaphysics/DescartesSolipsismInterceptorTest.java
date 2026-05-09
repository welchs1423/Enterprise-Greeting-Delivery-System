package com.egds.metaphysics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DescartesSolipsismInterceptorTest {

    private DescartesSolipsismInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new DescartesSolipsismInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    void preHandle_withValidProof_returnsTrue() throws Exception {
        when(request.getHeader(
                DescartesSolipsismInterceptor.COGITO_HEADER))
                .thenReturn("true");
        assertThat(interceptor.preHandle(
                request, response, new Object()))
                .isTrue();
    }

    @Test
    void preHandle_withAbsentHeader_throwsNonExistentClientException() {
        when(request.getHeader(
                DescartesSolipsismInterceptor.COGITO_HEADER))
                .thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        assertThatThrownBy(() ->
                interceptor.preHandle(
                        request, response, new Object()))
                .isInstanceOf(NonExistentClientException.class);
    }

    @Test
    void preHandle_withWrongValue_throwsNonExistentClientException() {
        when(request.getHeader(
                DescartesSolipsismInterceptor.COGITO_HEADER))
                .thenReturn("false");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        assertThatThrownBy(() ->
                interceptor.preHandle(
                        request, response, new Object()))
                .isInstanceOf(NonExistentClientException.class);
    }

    @Test
    void preHandle_withCaseInsensitiveTrue_returnsTrue() throws Exception {
        when(request.getHeader(
                DescartesSolipsismInterceptor.COGITO_HEADER))
                .thenReturn("TRUE");
        assertThat(interceptor.preHandle(
                request, response, new Object()))
                .isTrue();
    }

    @Test
    void nonExistentClientException_preservesMessage() {
        String msg = "client 1.2.3.4 failed proof-of-existence";
        NonExistentClientException ex =
                new NonExistentClientException(msg);
        assertThat(ex.getMessage()).isEqualTo(msg);
    }
}
