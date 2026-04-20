package com.egds.core.mapper;

import com.egds.core.dto.MessageContentDto;
import com.egds.core.entity.MessageEntity;
import java.time.Instant;
import java.util.UUID;

/**
 * Stateless mapper responsible for transforming validated {@link MessageContentDto}
 * payloads into output-ready {@link MessageEntity} instances.
 * This class enforces a strict separation between the transport layer (DTO) and the
 * domain representation (Entity) within the EGDS pipeline.
 */
public class MessageMapper {

    /**
     * Maps a {@link MessageContentDto} to a new {@link MessageEntity}.
     * The resulting entity is assigned a new UUID-based entity identifier
     * and initialized to {@link com.egds.core.enums.DeliveryStatus#PENDING}.
     *
     * @param dto the source data transfer object; must not be null
     * @return a fully initialized {@link MessageEntity} ready for pipeline progression
     * @throws IllegalArgumentException if the supplied DTO is null
     */
    public MessageEntity toEntity(MessageContentDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Source MessageContentDto must not be null.");
        }

        String entityId = UUID.randomUUID().toString();
        String formattedContent = formatContent(dto);
        long deliveryTimestamp = Instant.now().toEpochMilli();

        return new MessageEntity(entityId, dto.getCorrelationId(), formattedContent, deliveryTimestamp);
    }

    /**
     * Applies output-layer formatting to the raw message content.
     * Formatting incorporates the DTO's priority and locale metadata as structured prefixes.
     *
     * @param dto the source DTO from which formatted content is derived
     * @return a formatted string suitable for the output channel
     */
    private String formatContent(MessageContentDto dto) {
        return String.format("[%s][%s] %s",
                dto.getPriority().name(),
                dto.getLocale(),
                dto.getContent());
    }
}
