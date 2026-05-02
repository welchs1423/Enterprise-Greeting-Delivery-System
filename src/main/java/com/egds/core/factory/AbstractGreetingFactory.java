package com.egds.core.factory;

import com.egds.core.interfaces.IGreetingFactory;
import com.egds.core.interfaces.IMessageOutputStrategy;
import com.egds.core.interfaces.IMessageProvider;

/**
 * Abstract base class for EGDS delivery pipeline component factories.
 * Extends {@link IGreetingFactory} with shared lifecycle management and
 * factory identification semantics. Concrete subclasses must implement
 * component creation methods to produce a cohesive set of pipeline
 * artifacts.
 */
public abstract class AbstractGreetingFactory implements IGreetingFactory {

    /**
     * Returns the unique string identifier for this factory variant.
     * Used by {@link GreetingFactoryProvider} to resolve factory
     * selection at runtime.
     *
     * @return the factory type identifier string
     */
    public abstract String getFactoryType();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IMessageProvider createMessageProvider();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IMessageOutputStrategy createOutputStrategy();
}
