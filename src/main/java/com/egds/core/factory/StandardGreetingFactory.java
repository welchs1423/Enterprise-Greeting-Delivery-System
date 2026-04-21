package com.egds.core.factory;

import com.egds.core.dto.MessageContentDto;
import com.egds.core.enums.MessagePriority;
import com.egds.core.interfaces.IMessageOutputStrategy;
import com.egds.core.interfaces.IMessageProvider;
import com.egds.core.strategy.ConsoleOutputStrategy;

import java.util.UUID;

/**
 * Concrete Abstract Factory implementation producing the standard EGDS pipeline components.
 * Constructs a {@link HelloWorldMessageProvider} and a {@link ConsoleOutputStrategy},
 * representing the default operational configuration of the delivery pipeline.
 */
public class StandardGreetingFactory extends AbstractGreetingFactory {

    /**
     * The canonical type identifier for this factory variant.
     * Referenced by {@link GreetingFactoryProvider} during factory resolution.
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
     * Returns a minimal {@link IMessageProvider} for non-Spring (v1.0) environments.
     * In the Spring-managed context, {@code HelloWorldMessageProvider} is wired by the IoC
     * container and this factory method is not invoked.
     *
     * @return an inline provider that assembles the standard greeting without cache
     */
    @Override
    public IMessageProvider createMessageProvider() {
        return () -> new MessageContentDto.Builder("Hello, World!", UUID.randomUUID().toString())
                .locale("en-US")
                .priority(MessagePriority.NORMAL)
                .build();
    }

    /**
     * Creates a {@link ConsoleOutputStrategy} as the delivery output channel
     * for the standard delivery pipeline configuration.
     *
     * @return a new {@link ConsoleOutputStrategy} instance
     */
    @Override
    public IMessageOutputStrategy createOutputStrategy() {
        return new ConsoleOutputStrategy();
    }
}
