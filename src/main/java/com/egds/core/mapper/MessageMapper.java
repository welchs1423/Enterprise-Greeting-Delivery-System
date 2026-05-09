package com.egds.core.mapper;

import com.egds.core.dto.MessageContentDto;
import com.egds.core.entity.MessageEntity;
import com.egds.frontend.Vue3VirtualDomRenderer;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;

/**
 * Stateless mapper responsible for transforming validated
 * {@link MessageContentDto} payloads into output-ready
 * {@link MessageEntity} instances.
 * Enforces a strict separation between the transport layer (DTO) and the
 * domain representation (Entity) within the EGDS pipeline.
 * Content is serialized as a Vue 3 Virtual DOM node tree before storage.
 */
@Component
public class MessageMapper {

    /** Vue 3 Virtual DOM renderer applied during content formatting. */
    private final Vue3VirtualDomRenderer vDomRenderer;

    /**
     * @param renderer the Vue 3 Virtual DOM renderer for content wrapping
     */
    public MessageMapper(final Vue3VirtualDomRenderer renderer) {
        this.vDomRenderer = renderer;
    }

    /**
     * Maps a {@link MessageContentDto} to a new {@link MessageEntity}.
     * The resulting entity is assigned a new UUID-based entity identifier
     * and initialized to
     * {@link com.egds.core.enums.DeliveryStatus#PENDING}.
     *
     * @param dto the source data transfer object; must not be null
     * @return a fully initialized {@link MessageEntity}
     * @throws IllegalArgumentException if the supplied DTO is null
     */
    public MessageEntity toEntity(final MessageContentDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Source MessageContentDto must not be null.");
        }

        String entityId = UUID.randomUUID().toString();
        String formattedContent = formatContent(dto);
        long deliveryTimestamp = Instant.now().toEpochMilli();

        return new MessageEntity(
                entityId,
                dto.getCorrelationId(),
                formattedContent,
                deliveryTimestamp);
    }

    /**
     * Applies output-layer formatting to the raw message content and
     * serializes the result as a Vue 3 Virtual DOM node tree JSON string.
     * Incorporates the DTO's priority and locale metadata as prefixes
     * before wrapping in the VNode structure.
     *
     * @param dto the source DTO from which formatted content is derived
     * @return a VNode-wrapped JSON string suitable for the output channel
     */
    private String formatContent(final MessageContentDto dto) {
        String formatted = String.format("[%s][%s] %s",
                dto.getPriority().name(),
                dto.getLocale(),
                dto.getContent());
        return vDomRenderer.render(formatted);
    }
}
