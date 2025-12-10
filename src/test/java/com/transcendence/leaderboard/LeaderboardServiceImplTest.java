package com.transcendence.leaderboard;

import com.transcendence.leaderboard.dto.LeaderboardPlayerDTO;
import com.transcendence.leaderboard.dto.LeaderboardResponseDTO;
import com.transcendence.entity.User;
import com.transcendence.entity.UserStats;
import com.transcendence.stats.UserStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceImplTest {

    @Mock
    private UserStatsRepository userStatsRepository;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    private User mockUser1;
    private UserStats mockStats1;

    private User mockUser2;
    private UserStats mockStats2;

    @BeforeEach
    void setup() {
        mockUser1 = new User("Alpha", "a@example.com", "hash", null, "active");
        mockUser1.setId(1L);
        mockStats1 = UserStats.builder()
                .id(1L)
                .user(mockUser1)
                .totalGames(20)
                .wins(15)
                .losses(5)
                .winStreak(0)
                .bestStreak(5)
                .totalPlayTime(500)
                .rank(UserStats.Rank.Novice)
                .level(10)
                .xp(2000)
                .build();

        mockUser2 = new User("Beta", "b@example.com", "hash", null, "active");
        mockUser2.setId(2L);
        mockStats2 = UserStats.builder()
                .id(2L)
                .user(mockUser2)
                .totalGames(0)
                .wins(0)
                .losses(0)
                .winStreak(0)
                .bestStreak(0)
                .totalPlayTime(0)
                .rank(UserStats.Rank.Novice)
                .level(1)
                .xp(10)
                .build();
    }

    // ====================================================================
    // 1. Test getLeaderboard
    // ====================================================================

    @Test
    void getLeaderboard_shouldFetchAndMapCorrectly() {
        List<UserStats> statsList = Arrays.asList(mockStats1, mockStats2);
        when(userStatsRepository.findAllByOrderByXpDescLevelDesc())
                .thenReturn(statsList);

        LeaderboardResponseDTO resultDTO = leaderboardService.getLeaderboard();
        List<LeaderboardPlayerDTO> leaderboard = resultDTO.getLeaderboard();

        assertThat(resultDTO).isNotNull();
        assertThat(leaderboard).hasSize(2);

        LeaderboardPlayerDTO player1 = leaderboard.get(0);
        assertThat(player1.getUsername()).isEqualTo("Alpha");
        assertThat(player1.getLevel()).isEqualTo(10);
        assertThat(player1.getXp()).isEqualTo(2000L);
        assertThat(player1.getWinRate()).isEqualTo(75.0);

        LeaderboardPlayerDTO player2 = leaderboard.get(1);
        assertThat(player2.getUsername()).isEqualTo("Beta");
        assertThat(player2.getTotalGames()).isEqualTo(0);
        assertThat(player2.getWinRate()).isEqualTo(0.0);
        assertThat(player2.getRank()).isEqualTo("Novice");
    }

    @Test
    void getLeaderboard_whenEmpty_shouldReturnEmptyList() {
        when(userStatsRepository.findAllByOrderByXpDescLevelDesc())
                .thenReturn(List.of());

        LeaderboardResponseDTO resultDTO = leaderboardService.getLeaderboard();

        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.getLeaderboard()).isEmpty();
    }
}