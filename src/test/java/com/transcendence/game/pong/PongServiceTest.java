package com.transcendence.game.pong;

import com.transcendence.entity.PongMatch;
import com.transcendence.entity.User;
import com.transcendence.entity.UserStats;
import com.transcendence.game.pong.dto.PongHistoryItemDto;
import com.transcendence.game.pong.dto.PongHistoryResponse;
import com.transcendence.game.pong.dto.PongScoreRequest;
import com.transcendence.stats.UserStatsService;
import com.transcendence.stats.dto.SaveScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PongServiceTest {

    @InjectMocks
    private PongService pongService;

    @Mock
    private PongMatchRepository pongMatchRepository;

    private static final ZoneId TEST_ZONE = ZoneId.of("UTC");

    @Mock
    private UserStatsService userStatsService;

    private final Long USER_ID = 1L;
    private UserStats mockUserStats;
    private final User mockUser = new User("test_user", "t@example.com", "hash", null, "active");

    @BeforeEach
    void setup() {

        mockUserStats = UserStats.builder()
                .id(USER_ID)
                .user(mockUser)
                .totalGames(10)
                .wins(5)
                .losses(5)
                .winStreak(0)
                .bestStreak(0)
                .totalPlayTime(100)
                .rank(UserStats.Rank.Novice)
                .level(1)
                .xp(0)
                .build();
    }

    // ====================================================================
    // 1. Test saveScore - Opponent Wins (Mode: TWO_PLAYER, Duration null)
    // ====================================================================

    @Test
    void saveScore_opponentWins_shouldHandleNullDurationAndOpponentWinner() {

        String mode = "two-player";
        int score = 8;
        int opponentScore = 10;
        int xpEarned = 20;

        PongScoreRequest request = new PongScoreRequest();
        request.setScore(score);
        request.setOpponentScore(opponentScore);
        request.setWinner("opponent");
        request.setDuration(null);
        request.setXpEarned(xpEarned);
        request.setIsPerfectGame(false);

        when(userStatsService.updateAfterGame(USER_ID, false, 0, xpEarned))
                .thenReturn(mockUserStats);

        SaveScoreResponse response = pongService.saveScore(USER_ID, mode, request);

        ArgumentCaptor<PongMatch> matchCaptor = ArgumentCaptor.forClass(PongMatch.class);
        verify(pongMatchRepository, times(1)).save(matchCaptor.capture());

        PongMatch capturedMatch = matchCaptor.getValue();
        assertThat(capturedMatch.getMode()).isEqualTo(PongMatch.Mode.TWO_PLAYER);
        assertThat(capturedMatch.getWinner()).isEqualTo(PongMatch.Winner.opponent);
        assertThat(capturedMatch.getDuration()).isEqualTo(0); // Should default to 0

        verify(userStatsService, times(1)).updateAfterGame(
                eq(USER_ID),
                eq(false), // won = false
                eq(0), // duration passed as 0
                eq(xpEarned)
        );

        assertThat(response.getSuccess()).isTrue();
    }

    // ====================================================================
    // 2. Test getHistory - Matches exist
    // ====================================================================

    @Test
    void getHistory_whenMatchesExist_shouldReturnMappedHistory() {

        Instant now = Instant.now();

        LocalDateTime created1 = LocalDateTime.ofInstant(now.minusSeconds(3600), TEST_ZONE);
        LocalDateTime created2 = LocalDateTime.ofInstant(now, TEST_ZONE);

        PongMatch match1 = PongMatch.builder()
                .id(100L)
                .userId(USER_ID)
                .mode(PongMatch.Mode.ONE_PLAYER)
                .winner(PongMatch.Winner.player)
                .score(10).opponentScore(5)
                .duration(100)
                .xpEarned(20)
                .createdAt(created1)
                .build();

        PongMatch match2 = PongMatch.builder()
                .id(101L)
                .userId(USER_ID)
                .mode(PongMatch.Mode.TWO_PLAYER)
                .winner(PongMatch.Winner.opponent)
                .score(8).opponentScore(10)
                .duration(150)
                .xpEarned(10)
                .createdAt(created2)
                .build();

        List<PongMatch> mockMatches = List.of(match2, match1);

        when(pongMatchRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(mockMatches);

        PongHistoryResponse response = pongService.getHistory(USER_ID);

        verify(pongMatchRepository, times(1)).findByUserIdOrderByCreatedAtDesc(USER_ID);
        assertThat(response.getHistory()).hasSize(2);

        PongHistoryItemDto firstItem = response.getHistory().get(0);
        assertThat(firstItem.getScore()).isEqualTo(8);

        PongHistoryItemDto secondItem = response.getHistory().get(1);
        assertThat(secondItem.getScore()).isEqualTo(10);
    }

    // ====================================================================
    // 3. Test getHistory - No matches exist
    // ====================================================================

    @Test
    void getHistory_whenNoMatchesExist_shouldReturnEmptyHistory() {

        when(pongMatchRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(Collections.emptyList());

        PongHistoryResponse response = pongService.getHistory(USER_ID);

        verify(pongMatchRepository, times(1)).findByUserIdOrderByCreatedAtDesc(USER_ID);
        assertThat(response.getHistory()).isNotNull().isEmpty();
    }
}