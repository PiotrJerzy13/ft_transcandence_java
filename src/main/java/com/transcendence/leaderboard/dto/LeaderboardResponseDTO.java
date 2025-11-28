// LeaderboardResponseDTO.java
package com.transcendence.leaderboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponseDTO {

    private List<LeaderboardPlayerDTO> leaderboard;
    private String error;
}