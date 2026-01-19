package com.transcendence.leaderboard;

import com.transcendence.leaderboard.dto.LeaderboardPlayerDTO;
import com.transcendence.leaderboard.dto.LeaderboardResponseDTO;
import com.transcendence.entity.UserStats;
import com.transcendence.stats.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final UserStatsRepository userStatsRepository;

    @Override
    public LeaderboardResponseDTO getLeaderboard() {

        List<UserStats> topPlayers = userStatsRepository.findAllByOrderByXpDescLevelDesc();

        List<LeaderboardPlayerDTO> leaderboard = topPlayers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return LeaderboardResponseDTO.builder()
                .leaderboard(leaderboard)
                .build();
    }

    private LeaderboardPlayerDTO convertToDTO(UserStats stats) {
        // Calculate win rate as percentage
        double winRate = 0.0;
        if (stats.getTotalGames() > 0) {
            winRate = (stats.getWins() * 100.0) / stats.getTotalGames();
        }

        return LeaderboardPlayerDTO.builder()
                .id(stats.getUser().getId())
                .username(stats.getUser().getUsername())
                .level(stats.getLevel())
                .rank(stats.getRank().name()) // Convert enum to String
                .totalGames(stats.getTotalGames())
                .wins(stats.getWins())
                .losses(stats.getLosses())
                .xp(stats.getXp().longValue())
                .bestStreak(stats.getBestStreak())
                .winRate(winRate)
                .build();
    }
}
