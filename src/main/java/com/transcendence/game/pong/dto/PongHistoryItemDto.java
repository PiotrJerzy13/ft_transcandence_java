package com.transcendence.game.pong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.transcendence.entity.PongMatch;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PongHistoryItemDto {

    private Integer score;

    @JsonProperty("opponent_score")
    private Integer opponentScore;

    private String winner; // "player" or "opponent"

    @JsonProperty("xp")
    private Integer xpEarned;

    private Integer duration;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static PongHistoryItemDto fromEntity(PongMatch match) {
        PongHistoryItemDto dto = new PongHistoryItemDto();
        dto.setScore(match.getScore());
        dto.setOpponentScore(match.getOpponentScore());
        dto.setWinner(match.getWinner().name()); // Winner enum → string
        dto.setXpEarned(match.getXpEarned());
        dto.setDuration(match.getDuration());
        dto.setCreatedAt(match.getCreatedAt());
        return dto;
    }
}
