package com.transcendence.game.arkanoid.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.transcendence.entity.ArkanoidScore;
import lombok.*;

import java.time.LocalDateTime;
@Data
@Builder
public class ArkanoidHistoryItemDto {

    private Integer score;

    @JsonProperty("level_reached")
    private Integer levelReached;

    private Integer xp;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ArkanoidHistoryItemDto fromEntity(ArkanoidScore entity) {
        return ArkanoidHistoryItemDto.builder()
                .score(entity.getScore())
                .levelReached(entity.getLevelReached())
                .xp(entity.getXpEarned())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
