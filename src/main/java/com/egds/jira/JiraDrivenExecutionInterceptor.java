package com.egds.jira;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP aspect enforcing a mandatory JIRA ticket lifecycle before any
 * {@code MessageDeliveryPipeline.execute()} invocation proceeds.
 *
 * <p>Each interception creates a mock JIRA ticket, transitions it
 * through IN_PROGRESS to DONE with a simulated ~50 ms API delay,
 * then allows the underlying method to execute. No code in the
 * intercepted pointcut runs without completing this lifecycle
 * sequence.</p>
 */
@Aspect
@Component
public class JiraDrivenExecutionInterceptor {

    /** Logger for JIRA lifecycle events. */
    private static final Logger LOG =
            LoggerFactory.getLogger(
                    JiraDrivenExecutionInterceptor.class);

    /** Simulated JIRA API round-trip delay in milliseconds. */
    private static final long JIRA_DELAY_MS = 50L;

    /** Prefix for generated ticket identifiers. */
    private static final String TICKET_PREFIX = "EGDS-";

    /** Modulus for generating numeric ticket suffixes. */
    private static final long TICKET_ID_MOD = 100_000L;

    /**
     * Intercepts {@code MessageDeliveryPipeline.execute()} and
     * enforces a mock JIRA ticket lifecycle before the method
     * proceeds.
     *
     * @param pjp the proceeding join point
     * @return the return value of the wrapped method
     * @throws Throwable if the underlying method throws
     */
    @Around("execution(* com.egds.core.pipeline"
            + ".MessageDeliveryPipeline.execute(..))")
    public Object aroundPipelineExecute(
            final ProceedingJoinPoint pjp) throws Throwable {
        String ticketId = openTicket(
                pjp.getSignature().getName());
        transitionStatus(ticketId, "IN_PROGRESS");
        sleepForApiDelay();
        transitionStatus(ticketId, "DONE");
        return pjp.proceed();
    }

    /**
     * Creates a mock JIRA ticket and returns its identifier.
     *
     * @param methodName the intercepted method name as ticket summary
     * @return the generated ticket identifier
     */
    private String openTicket(final String methodName) {
        long suffix = System.currentTimeMillis() % TICKET_ID_MOD;
        String ticketId = TICKET_PREFIX + suffix;
        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "jira_event=CREATE ticket={} summary=\"{}\"",
                    ticketId, methodName);
        }
        return ticketId;
    }

    /**
     * Transitions the given ticket to the specified status.
     *
     * @param ticketId the ticket identifier to transition
     * @param status   the target status string
     */
    private void transitionStatus(
            final String ticketId, final String status) {
        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "jira_event=TRANSITION ticket={} status={}",
                    ticketId, status);
        }
    }

    /**
     * Sleeps for the configured JIRA API simulation delay.
     */
    private void sleepForApiDelay() {
        try {
            Thread.sleep(JIRA_DELAY_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
