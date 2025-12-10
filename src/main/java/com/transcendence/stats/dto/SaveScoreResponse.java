package com.transcendence.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SaveScoreResponse {
    private Boolean success;
    private String message;
    private List<Object> newAchievements;  // Always empty array for now
    private UserStatsDto userStats;
}
