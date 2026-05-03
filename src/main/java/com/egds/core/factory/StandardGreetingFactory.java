package com.egds.core.factory;

import com.egds.core.dto.MessageContentDto;
import com.egds.core.enums.MessagePriority;
import com.egds.core.interfaces.IMessageOutputStrategy;
import com.egds.core.interfaces.IMessageProvider;
import com.egds.core.strategy.ConsoleOutputStrategy;

import java.util.UUID;

/**
 * Concrete Abstract Factory implementation producing the standard EGDS
 * pipeline components. Constructs a {@code HelloWorldMessageProvider} and
 * a {@link ConsoleOutputStrategy}, representing the default operational
 * configuration of the delivery pipeline.
 */
public class StandardGreetingFactory extends AbstractGreetingFactory {

    /**
     * Canonical type identifier for this factory variant.
     * Referenced by {@link GreetingFactoryProvider} during resolution.
     */
    public static final String FACTORY_TYPE = "STANDARD";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFactoryType() {
        return FACTORY_TYPE;
    }

    /**
     * Returns a minimal {@link IMessageProvider} for non-Spring
     * environments. In the Spring-managed context,
     * {@code HelloWorldMessageProvider} is wired by the IoC container
     * and this factory method is not invoked.
     *
     * @return an inline provider that assembles the greeting without cache
     */
    @Override
    public IMessageProvider createMessageProvider() {
        return () -> new MessageContentDto.Builder(
                "Hello, World!", UUID.randomUUID().toString())
                .locale("en-US")
                .priority(MessagePriority.NORMAL)
                .build();
    }

    /**
     * Not supported in standalone mode.
     *
     * <p>{@link ConsoleOutputStrategy} requires Spring-managed
     * {@code Tracer} and {@code GreetingIntegrityVerifier} dependencies
     * and cannot be instantiated outside the IoC container. In all
     * operational deployments this method is never reached because Spring
     * wires {@code ConsoleOutputStrategy} directly.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public IMessageOutputStrategy createOutputStrategy() {
        throw new UnsupportedOperationException(
                "ConsoleOutputStrategy requires Spring-managed dependencies;"
                        + " obtain the bean from the ApplicationContext.");
    }
}
