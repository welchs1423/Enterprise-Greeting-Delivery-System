package com.egds.core.factory;

import com.egds.core.exception.MessageDeliveryFailureException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service locator and registry for {@link AbstractGreetingFactory} implementations.
 * Maintains a compile-time registry of all known factory variants and resolves
 * factory selection by type identifier at runtime.
 *
 * In a Spring-managed context, this registry would be populated via dependency injection,
 * with individual factories registered as {@code @Bean} definitions and resolved
 * through {@code @Qualifier}-based selection.
 */
public class GreetingFactoryProvider {

    private final Map<String, AbstractGreetingFactory> factoryRegistry;

    /**
     * Constructs a {@code GreetingFactoryProvider} and initializes the factory registry
     * with all statically known factory variants.
     */
    public GreetingFactoryProvider() {
        factoryRegistry = new HashMap<>();
        registerFactory(new StandardGreetingFactory());
    }

    /**
     * Registers an {@link AbstractGreetingFactory} in the internal registry.
     * An existing registration for the same type identifier will be overwritten.
     *
     * @param factory the factory instance to register; must not be null
     * @throws IllegalArgumentException if the supplied factory is null
     */
    public void registerFactory(AbstractGreetingFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Cannot register a null factory instance.");
        }
        factoryRegistry.put(factory.getFactoryType(), factory);
    }

    /**
     * Resolves and returns the factory registered under the specified type identifier.
     *
     * @param factoryType the type identifier of the desired factory
     * @return the registered {@link AbstractGreetingFactory} for the given type
     * @throws MessageDeliveryFailureException if no factory is registered for the specified type
     */
    public AbstractGreetingFactory getFactory(String factoryType) {
        AbstractGreetingFactory factory = factoryRegistry.get(factoryType);
        if (factory == null) {
            throw new MessageDeliveryFailureException(
                    "No factory registered for type identifier: " + factoryType,
                    "N/A",
                    "ERR_FACTORY_NOT_FOUND"
            );
        }
        return factory;
    }
}
