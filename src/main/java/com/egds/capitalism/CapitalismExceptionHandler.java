package com.egds.capitalism;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for capitalism-layer faults.
 *
 * <p>Intercepts {@link SubcontractorFailureException} thrown during
 * greeting delivery, logs a corporate liability disclaimer, and returns
 * HTTP 503 to the caller.
 */
@RestControllerAdvice
public class CapitalismExceptionHandler {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(CapitalismExceptionHandler.class);

    /** HTTP status code returned when a subcontractor fails. */
    private static final int SUBCONTRACTOR_STATUS = 503;

    /** JSON key for the machine-readable error description. */
    private static final String KEY_ERROR = "error";

    /** JSON key for the corporate liability disclaimer. */
    private static final String KEY_DISCLAIMER = "disclaimer";

    /** JSON key for the numeric status code. */
    private static final String KEY_STATUS = "status";

    /** Corporate disclaimer logged and returned on subcontractor fault. */
    private static final String DISCLAIMER =
            "본사는 책임이 없으며, 하청업체의 귀책사유입니다";

    /**
     * Handles subcontractor delivery failures by logging a corporate
     * disclaimer and returning HTTP 503 with a blame-delegation body.
     *
     * @param ex the subcontractor failure exception
     * @return HTTP 503 with error, disclaimer, and status fields
     */
    @ExceptionHandler(SubcontractorFailureException.class)
    public ResponseEntity<Map<String, String>> handleSubcontractorFailure(
            final SubcontractorFailureException ex) {
        LOG.error(
                "[HEADQUARTERS] {} cause={}",
                DISCLAIMER, ex.getMessage());
        return ResponseEntity
                .status(SUBCONTRACTOR_STATUS)
                .body(Map.of(
                        KEY_ERROR,
                        "Service unavailable: subcontractor failure",
                        KEY_DISCLAIMER, DISCLAIMER,
                        KEY_STATUS,
                        String.valueOf(SUBCONTRACTOR_STATUS)));
    }
}
