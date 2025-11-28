
package com.transcendence.leaderboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardPlayerDTO {

    private Long id;
    private String username;
    private Integer level;
    private String rank;
    private Integer totalGames;
    private Integer wins;
    private Integer losses;
    private Long xp;
    private Integer bestStreak;
    private Double winRate;
}