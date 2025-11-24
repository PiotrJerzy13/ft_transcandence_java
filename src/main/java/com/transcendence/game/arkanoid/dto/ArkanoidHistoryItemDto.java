package com.transcendence.game.arkanoid.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.transcendence.entity.ArkanoidScore;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ArkanoidHistoryItemDto {

    private Integer score;

    @JsonProperty("level_reached")
    private Integer levelReached;

    private Integer xp;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ArkanoidHistoryItemDto fromEntity(ArkanoidScore entity) {
        ArkanoidHistoryItemDto dto = new ArkanoidHistoryItemDto();
        dto.setScore(entity.getScore());
        dto.setLevelReached(entity.getLevelReached());
        dto.setXp(entity.getXpEarned());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
