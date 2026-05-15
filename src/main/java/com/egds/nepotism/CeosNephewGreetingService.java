package com.egds.nepotism;

import com.egds.core.dto.MessageContentDto;
import com.egds.core.interfaces.IMessageProvider;
import com.egds.core.provider.HelloWorldMessageProvider;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@link IMessageProvider} implementation representing the CEO's
 * nephew, injected through executive mandate.
 *
 * <p>Delegates to the standard provider for most requests but substitutes
 * a nepotism greeting with 30% probability when enabled.
 * Wrapped by {@link com.egds.lootbox.GreetingLootboxProvider} (V21),
 * which holds the {@code @Primary} designation in the delivery chain.
 *
 * <p>Set {@code egds.nepotism.enabled=false} to disable the substitution
 * in test environments.
 */
@Component
public class CeosNephewGreetingService implements IMessageProvider {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(CeosNephewGreetingService.class);

    /** Probability numerator for nephew greeting substitution. */
    private static final int NEPHEW_THRESHOLD = 3;

    /** Denominator for percentage-based substitution probability. */
    private static final int PERCENT = 10;

    /** BCP 47 locale tag applied to nephew greeting messages. */
    private static final String LOCALE = "en-US";

    /** Greeting content emitted when nephew substitution is active. */
    private static final String NEPHEW_GREETING =
            "Sup world (by nephew)";

    /** Standard provider delegated to when nephew mode is inactive. */
    private final HelloWorldMessageProvider delegate;

    /**
     * When false, nephew substitution never fires; all calls are
     * forwarded to {@link HelloWorldMessageProvider}.
     */
    @Value("${egds.nepotism.enabled:true}")
    private boolean nepotismEnabled;

    /**
     * @param helloWorldProvider the standard greeting provider
     */
    public CeosNephewGreetingService(
            final HelloWorldMessageProvider helloWorldProvider) {
        this.delegate = helloWorldProvider;
    }

    /**
     * Returns a nepotism greeting with 30% probability when enabled;
     * otherwise delegates to {@link HelloWorldMessageProvider}.
     *
     * @return a {@link MessageContentDto} carrying the selected greeting
     */
    @Override
    public MessageContentDto provideMessage() {
        if (nepotismEnabled
                && ThreadLocalRandom.current().nextInt(PERCENT)
                        < NEPHEW_THRESHOLD) {
            String corrId = UUID.randomUUID().toString();
            if (LOG.isInfoEnabled()) {
                LOG.info(
                        "[NEPOTISM] Nephew override active corrId={}",
                        corrId);
            }
            return new MessageContentDto.Builder(
                    NEPHEW_GREETING, corrId)
                    .locale(LOCALE)
                    .build();
        }
        return delegate.provideMessage();
    }
}
