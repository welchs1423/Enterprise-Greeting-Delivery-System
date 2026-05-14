package com.egds.consulting;

import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP aspect that simulates overhead introduced by an external consulting firm.
 *
 * <p>Intercepts greeting controller method invocations, performs a no-op
 * SOAP XML serialization round-trip to demonstrate protocol transformation
 * overhead, blocks for 1000 ms to model consulting review latency, and
 * injects the {@code X-Synergy-Aligned: true} response header to signal
 * strategic alignment.
 *
 * <p>Set {@code egds.consulting.enabled=false} in test properties to
 * suppress the delay in automated tests.
 */
@Aspect
@Component
public class McKinseyConsultingProxy {

    /** Logger for this aspect. */
    private static final Logger LOG =
            LoggerFactory.getLogger(McKinseyConsultingProxy.class);

    /** SOAP 1.1 envelope namespace URI. */
    private static final String SOAP_NS_URI =
            "http://schemas.xmlsoap.org/soap/envelope/";

    /** Response header name injected on every intercepted call. */
    private static final String SYNERGY_HEADER = "X-Synergy-Aligned";

    /** Value written to the synergy response header. */
    private static final String SYNERGY_VALUE = "true";

    /** Delay in milliseconds applied per intercepted invocation. */
    private static final long CONSULTING_DELAY_MS = 1000L;

    /**
     * When false, the aspect passes through without delay or header
     * injection. Controlled by {@code egds.consulting.enabled}.
     */
    @Value("${egds.consulting.enabled:true}")
    private boolean consultingEnabled;

    /**
     * Wraps greeting controller methods with consulting overhead:
     * a mock SOAP round-trip, a 1000 ms delay, and synergy header injection.
     *
     * @param pjp the proceeding join point
     * @return the return value of the intercepted method
     * @throws Throwable if the underlying method throws
     */
    @Around("execution(* com.egds.web.GreetingController.*(..))"
            + " || execution("
            + "* com.egds.web.GreetingQueryController.*(..))")
    public Object aroundGreetingMethods(
            final ProceedingJoinPoint pjp) throws Throwable {
        if (!consultingEnabled) {
            return pjp.proceed();
        }
        String mockPayload = serializeToSoap("synergy-aligned-payload");
        deserializeFromSoap(mockPayload);
        applyConsultingDelay();
        injectSynergyHeader();
        return pjp.proceed();
    }

    /**
     * Blocks the calling thread for {@link #CONSULTING_DELAY_MS}
     * milliseconds to simulate consulting review latency.
     */
    void applyConsultingDelay() {
        LOG.info("[CONSULTING] Strategic framework alignment in progress");
        try {
            TimeUnit.MILLISECONDS.sleep(CONSULTING_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Injects the {@code X-Synergy-Aligned: true} header into the current
     * servlet response if a request context is available.
     */
    void injectSynergyHeader() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }
        HttpServletResponse response = attrs.getResponse();
        if (response != null) {
            response.setHeader(SYNERGY_HEADER, SYNERGY_VALUE);
        }
    }

    /**
     * Wraps {@code payload} in a minimal SOAP 1.1 envelope.
     *
     * @param payload the string content to wrap
     * @return SOAP XML envelope string containing the payload
     */
    String serializeToSoap(final String payload) {
        return "<soapenv:Envelope xmlns:soapenv=\""
                + SOAP_NS_URI + "\">"
                + "<soapenv:Body>"
                + "<egds:payload>"
                + payload
                + "</egds:payload>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
    }

    /**
     * Strips all XML tags from {@code xml} to recover the original payload.
     *
     * @param xml the SOAP XML string to deserialize
     * @return the text content with all XML tags removed and whitespace trimmed
     */
    String deserializeFromSoap(final String xml) {
        return xml.replaceAll("<[^>]+>", "").trim();
    }
}
