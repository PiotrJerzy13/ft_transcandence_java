package com.transcendence.stats;

import com.transcendence.entity.User;
import com.transcendence.entity.UserStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserStatsService {

    private final UserStatsRepository userStatsRepository;

    public UserStatsService(UserStatsRepository userStatsRepository) {
        this.userStatsRepository = userStatsRepository;
    }

    public UserStats createInitialStats(User user) {
        UserStats stats = UserStats.builder()
                .user(user)
                .totalGames(0)
                .wins(0)
                .losses(0)
                .winStreak(0)
                .bestStreak(0)
                .totalPlayTime(0)
                .rank(UserStats.Rank.Novice)
                .level(1)
                .xp(0)
                .build();

        return userStatsRepository.save(stats);
    }

    public UserStats updateAfterGame(Long userId, boolean won, int duration, int xpEarned) {

        UserStats stats = userStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User stats not found"));

        // basic stats
        stats.setTotalGames(stats.getTotalGames() + 1);
        stats.setWins(stats.getWins() + (won ? 1 : 0));
        stats.setLosses(stats.getLosses() + (won ? 0 : 1));

        // streaks
        int newWinStreak = won ? stats.getWinStreak() + 1 : 0;
        stats.setWinStreak(newWinStreak);
        stats.setBestStreak(Math.max(stats.getBestStreak(), newWinStreak));

        stats.setTotalPlayTime(stats.getTotalPlayTime() + duration);

        int newXp = stats.getXp() + xpEarned;
        stats.setXp(newXp);

        int newLevel = (int) Math.floor(Math.sqrt(newXp / 100.0)) + 1;
        stats.setLevel(newLevel);

        stats.setRank(UserStats.Rank.Novice);

        return userStatsRepository.save(stats);
    }
}
