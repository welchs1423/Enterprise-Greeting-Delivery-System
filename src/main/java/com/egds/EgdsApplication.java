package com.egds;

import com.egds.core.aspect.MessageDeliveryLoggingAspect;
import com.egds.core.factory.AbstractGreetingFactory;
import com.egds.core.factory.GreetingFactoryProvider;
import com.egds.core.factory.StandardGreetingFactory;
import com.egds.core.mapper.MessageMapper;
import com.egds.core.pipeline.MessageDeliveryPipeline;
import com.egds.core.validator.MessageContentValidator;

/**
 * Bootstrap entry point for the Enterprise Greeting Delivery System (EGDS).
 * Initializes the factory registry, resolves the appropriate factory variant,
 * assembles the delivery pipeline, and delegates execution to the pipeline facade.
 *
 * In a production Spring Boot context, component wiring would be managed
 * by the IoC container via constructor injection and {@code @Configuration} classes.
 * This class would be annotated with {@code @SpringBootApplication} and the manual
 * instantiation below would be replaced by container-managed beans.
 */
public class EgdsApplication {

    /**
     * Application entry point. Bootstraps the EGDS delivery pipeline.
     *
     * @param args command-line arguments; not consumed by the current pipeline implementation
     */
    public static void main(String[] args) {
        GreetingFactoryProvider factoryProvider = new GreetingFactoryProvider();
        AbstractGreetingFactory factory = factoryProvider.getFactory(StandardGreetingFactory.FACTORY_TYPE);

        MessageDeliveryPipeline pipeline = new MessageDeliveryPipeline(
                factory.createMessageProvider(),
                factory.createOutputStrategy(),
                new MessageMapper(),
                new MessageContentValidator(),
                new MessageDeliveryLoggingAspect()
        );

        pipeline.execute();
    }
}
