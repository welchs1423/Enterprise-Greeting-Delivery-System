package com.egds.metaphysics;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that the originating HTTP client could not be confirmed as a
 * conscious, existent entity. Thrown by
 * {@link DescartesSolipsismInterceptor} when the required
 * proof-of-existence header is absent or carries an invalid value.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class NonExistentClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message description of the existence-proof failure
     */
    public NonExistentClientException(final String message) {
        super(message);
    }
}
