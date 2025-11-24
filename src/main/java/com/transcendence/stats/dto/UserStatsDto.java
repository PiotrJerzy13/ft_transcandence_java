package com.transcendence.stats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.transcendence.entity.UserStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserStatsDto {

    private Long id;
//JsonProperty because frontend expects snake_case
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("total_games")
    private Integer totalGames;

    private Integer wins;
    private Integer losses;

    @JsonProperty("win_streak")
    private Integer winStreak;

    @JsonProperty("best_streak")
    private Integer bestStreak;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private Integer level;
    private String rank;

    @JsonProperty("total_play_time")
    private Integer totalPlayTime;

    private Integer xp;

    // Factory method to convert entity to DTO
    public static UserStatsDto fromEntity(UserStats stats) {
        return UserStatsDto.builder()
                .id(stats.getId())
                .userId(stats.getUser().getId())
                .totalGames(stats.getTotalGames())
                .wins(stats.getWins())
                .losses(stats.getLosses())
                .winStreak(stats.getWinStreak())
                .bestStreak(stats.getBestStreak())
                .createdAt(stats.getCreatedAt())
                .updatedAt(stats.getUpdatedAt())
                .level(stats.getLevel())
                .rank(stats.getRank().name())
                .totalPlayTime(stats.getTotalPlayTime())
                .xp(stats.getXp())
                .build();
    }
}
