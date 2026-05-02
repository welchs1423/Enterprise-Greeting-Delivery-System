package com.egds.core.interfaces;

/**
 * Abstract Factory contract for creation of EGDS delivery pipeline
 * components. Implementations produce a compatible set of
 * {@link IMessageProvider} and {@link IMessageOutputStrategy} instances,
 * ensuring cohesion between the message source and output channel.
 */
public interface IGreetingFactory {

    /**
     * Creates and returns the {@link IMessageProvider} associated with
     * this factory variant.
     *
     * @return a non-null {@link IMessageProvider} instance
     */
    IMessageProvider createMessageProvider();

    /**
     * Creates and returns the {@link IMessageOutputStrategy} associated
     * with this factory variant.
     *
     * @return a non-null {@link IMessageOutputStrategy} instance
     */
    IMessageOutputStrategy createOutputStrategy();
}
