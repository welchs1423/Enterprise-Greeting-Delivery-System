package com.egds.core.factory;

import com.egds.core.interfaces.IMessageOutputStrategy;
import com.egds.core.interfaces.IMessageProvider;
import com.egds.core.provider.HelloWorldMessageProvider;
import com.egds.core.strategy.ConsoleOutputStrategy;

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
     * Creates a {@link HelloWorldMessageProvider} as the message content source
     * for the standard delivery pipeline configuration.
     *
     * @return a new {@link HelloWorldMessageProvider} instance
     */
    @Override
    public IMessageProvider createMessageProvider() {
        return new HelloWorldMessageProvider();
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
