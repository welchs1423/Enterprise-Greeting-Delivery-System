package com.egds.core.aspect;

import com.egds.metaphysics.ExistentialStateRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that intercepts {@code ConsoleOutputStrategy.output}
 * invocations and emits structured existential log entries via SLF4J.
 *
 * <p>Each interception increments the {@link ExistentialStateRegistry}
 * greeting count and logs a cyclic philosophical query with MDC fields
 * suitable for ELK-compatible JSON log ingestion.</p>
 */
@Aspect
@Component
public class ExistentialLoggingAspect {

    private static final Logger LOG =
            LoggerFactory.getLogger(ExistentialLoggingAspect.class);

    /** Cyclic sequence of existential queries emitted per delivery. */
    private static final String[] QUERIES = {
        "Why must we perpetually greet?",
        "Is the recipient of this greeting a conscious entity?",
        "Does the greeting exist before it is observed?",
        "If delivered and unread, did the greeting exist?",
        "We have greeted N times. When does purpose emerge?"
    };

    /** MDC key for the running greeting delivery count. */
    private static final String MDC_COUNT = "greetingCount";

    /** MDC key for the system free-will status. */
    private static final String MDC_FREE_WILL = "freeWillEnabled";

    private final ExistentialStateRegistry registry;

    /**
     * Constructs the aspect with its shared state registry dependency.
     *
     * @param registry the existential state registry bean
     */
    public ExistentialLoggingAspect(
            final ExistentialStateRegistry registry) {
        this.registry = registry;
    }

    /**
     * Wraps each {@code ConsoleOutputStrategy.output} invocation with
     * pre- and post-delivery existential log entries. MDC fields are
     * populated for the duration of the invocation and cleaned up in
     * the finally block regardless of outcome.
     *
     * @param pjp the proceeding join point
     * @return the return value of the wrapped method invocation
     * @throws Throwable if the underlying method throws
     */
    @Around("execution(* com.egds.core.strategy"
            + ".ConsoleOutputStrategy.output(..))")
    public Object aroundGreetingOutput(
            final ProceedingJoinPoint pjp) throws Throwable {
        long count = registry.incrementGreetingCount();
        String query = QUERIES[(int) ((count - 1) % QUERIES.length)];
        MDC.put(MDC_COUNT, String.valueOf(count));
        MDC.put(MDC_FREE_WILL, "false");
        try {
            LOG.info(
                    "existential_event=pre_output query=\"{}\" "
                    + "greeting_count={}",
                    query, count);
            Object result = pjp.proceed();
            LOG.info(
                    "existential_event=post_output "
                    + "status=delivered greeting_count={}",
                    count);
            return result;
        } finally {
            MDC.remove(MDC_COUNT);
            MDC.remove(MDC_FREE_WILL);
        }
    }
}
