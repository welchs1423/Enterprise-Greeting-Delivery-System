package com.egds.lootbox;

import com.egds.blockchain.GreetingIntegrityVerifier;
import com.egds.core.dto.MessageContentDto;
import com.egds.core.enums.MessagePriority;
import com.egds.core.interfaces.IMessageProvider;
import com.egds.nepotism.CeosNephewGreetingService;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * V21 확률형 인사말 가챠 프로바이더.
 *
 * <p>기존 {@link CeosNephewGreetingService}를 래핑하여 극악의
 * 확률 구조를 적용합니다. 80%는 "Hi", 19%는 "Hello",
 * 단 1%만이 온전한 "Hello, World!"를 반환하는 가챠 로직입니다.
 *
 * <p>비당첨 결과("Hi", "Hello")는 독립된 correlationId와
 * Keccak-256 해시를 생성하여 {@link GreetingIntegrityVerifier}에
 * 등록합니다. 1% 잭팟 시에는 {@link CeosNephewGreetingService}에
 * 그대로 위임하여 AI 및 블록체인 파이프라인을 완주합니다.
 *
 * <p>{@code egds.lootbox.enabled=false}로 설정하면 가챠가
 * 비활성화되어 항상 {@link CeosNephewGreetingService}에 위임합니다
 * (테스트 환경 결정론적 동작 보장용).
 */
@Primary
@Component
public class GreetingLootboxProvider implements IMessageProvider {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingLootboxProvider.class);

    /** Denominator for percentage-based gacha probability. */
    private static final int PERCENT = 100;

    /** Threshold below which a common "Hi" drop is returned (80%). */
    private static final int HI_THRESHOLD = 80;

    /** Threshold below which a rare "Hello" drop is returned (99%). */
    private static final int HELLO_THRESHOLD = 99;

    /** Fixed locale tag applied to all lootbox results. */
    private static final String LOCALE = "en-US";

    /** Fixed priority applied to all lootbox results. */
    private static final MessagePriority PRIORITY = MessagePriority.NORMAL;

    /** Common-tier drop content: 80% probability. */
    private static final String GREETING_COMMON = "Hi";

    /** Rare-tier drop content: 19% probability. */
    private static final String GREETING_RARE = "Hello";

    /** Jackpot-path provider for the 1% full greeting delegation. */
    private final CeosNephewGreetingService ceosNephewService;

    /** Blockchain verifier for pre-delivery content fingerprinting. */
    private final GreetingIntegrityVerifier integrityVerifier;

    /**
     * When false, gacha is disabled and all calls delegate to
     * {@link CeosNephewGreetingService} unconditionally.
     */
    @Value("${egds.lootbox.enabled:true}")
    private boolean lootboxEnabled;

    /**
     * @param ceosNephew the jackpot-path CEO's nephew greeting provider
     * @param verifier   the blockchain integrity verifier
     */
    public GreetingLootboxProvider(
            final CeosNephewGreetingService ceosNephew,
            final GreetingIntegrityVerifier verifier) {
        this.ceosNephewService = ceosNephew;
        this.integrityVerifier = verifier;
    }

    /**
     * Executes the gacha roll and returns the resulting greeting DTO.
     * When lootbox is disabled, delegates unconditionally to the
     * {@link CeosNephewGreetingService} for deterministic test output.
     *
     * @return a fully populated {@link MessageContentDto}
     */
    @Override
    public MessageContentDto provideMessage() {
        if (!lootboxEnabled) {
            return ceosNephewService.provideMessage();
        }
        int roll = rollDice();
        if (roll < HI_THRESHOLD) {
            return buildResult(GREETING_COMMON, "COMMON", roll);
        } else if (roll < HELLO_THRESHOLD) {
            return buildResult(GREETING_RARE, "RARE", roll);
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("[LOOTBOX] JACKPOT! roll={}", roll);
            }
            return ceosNephewService.provideMessage();
        }
    }

    /**
     * Returns a random integer in [0, PERCENT). Overridable in tests
     * to produce deterministic roll outcomes.
     *
     * @return the dice roll value
     */
    int rollDice() {
        return ThreadLocalRandom.current().nextInt(PERCENT);
    }

    /**
     * Registers the Keccak-256 hash for the drop result and
     * constructs the delivery DTO.
     *
     * @param content the greeting text for this drop tier
     * @param rarity  the tier label used for structured logging
     * @param roll    the dice roll value used for structured logging
     * @return the populated {@link MessageContentDto}
     */
    private MessageContentDto buildResult(
            final String content,
            final String rarity,
            final int roll) {
        String correlationId = UUID.randomUUID().toString();
        String preFormatted = String.format(
                "[%s][%s] %s", PRIORITY.name(), LOCALE, content);
        integrityVerifier.register(correlationId, preFormatted);
        if (LOG.isInfoEnabled()) {
            LOG.info("[LOOTBOX] {} drop. rarity={} roll={}",
                    content, rarity, roll);
        }
        return new MessageContentDto.Builder(content, correlationId)
                .locale(LOCALE)
                .priority(PRIORITY)
                .build();
    }
}
