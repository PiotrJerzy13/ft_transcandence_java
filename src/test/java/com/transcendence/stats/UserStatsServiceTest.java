package com.transcendence.stats;

import com.transcendence.entity.User;
import com.transcendence.entity.UserStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceTest {

    @Mock
    private UserStatsRepository userStatsRepository;

    @InjectMocks
    private UserStatsService userStatsService;

    private static final Long TEST_USER_ID = 1L;
    private User mockUser;
    private UserStats initialStats;

    @BeforeEach
    void setup() {
        mockUser = new User("test_user", "t@example.com", "hash", null, "active");
        mockUser.setId(TEST_USER_ID);

        initialStats = UserStats.builder()
                .id(TEST_USER_ID)
                .user(mockUser)
                .totalGames(10)
                .wins(5)
                .losses(5)
                .winStreak(0)
                .bestStreak(2)
                .totalPlayTime(100)
                .rank(UserStats.Rank.Novice)
                .level(1)
                .xp(0)
                .build();
    }

    // ====================================================================
    // 1. Test createInitialStats
    // ====================================================================

    @Test
    void createInitialStats_shouldBuildAndSaveDefaultStats() {

        // capture the object passed to the save method
        ArgumentCaptor<UserStats> statsCaptor = ArgumentCaptor.forClass(UserStats.class);

        when(userStatsRepository.save(statsCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserStats resultStats = userStatsService.createInitialStats(mockUser);

        verify(userStatsRepository, times(1)).save(any(UserStats.class));
        assertThat(resultStats.getUser()).isEqualTo(mockUser);
        assertThat(resultStats.getTotalGames()).isEqualTo(0);
        assertThat(resultStats.getLevel()).isEqualTo(1);
    }

    // ====================================================================
    // 2. Test updateAfterGame - Win Logic
    // ====================================================================

    @Test
    void updateAfterGame_whenWon_shouldIncrementWinAndStreak() {
        when(userStatsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(initialStats));

        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStats updatedStats = userStatsService.updateAfterGame(TEST_USER_ID, true, 10, 100);

        assertThat(updatedStats.getTotalGames()).isEqualTo(11);
        assertThat(updatedStats.getWins()).isEqualTo(6);
        assertThat(updatedStats.getLosses()).isEqualTo(5);

        assertThat(updatedStats.getWinStreak()).isEqualTo(1);
        assertThat(updatedStats.getBestStreak()).isEqualTo(2);

        assertThat(updatedStats.getXp()).isEqualTo(100);
        assertThat(updatedStats.getLevel()).isEqualTo(2);

        assertThat(updatedStats.getTotalPlayTime()).isEqualTo(110);
        verify(userStatsRepository, times(1)).save(updatedStats);
    }

    @Test
    void updateAfterGame_whenWonAndExistingStreak_shouldUpdateBestStreak() {
        initialStats.setWinStreak(2);
        when(userStatsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(initialStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStats updatedStats = userStatsService.updateAfterGame(TEST_USER_ID, true, 1, 1);

        assertThat(updatedStats.getWinStreak()).isEqualTo(3);
        assertThat(updatedStats.getBestStreak()).isEqualTo(3);
    }

    // ====================================================================
    // 3. Test updateAfterGame - Loss Logic
    // ====================================================================

    @Test
    void updateAfterGame_whenLost_shouldIncrementLossAndResetStreak() {
        initialStats.setWinStreak(2);
        when(userStatsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(initialStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStats updatedStats = userStatsService.updateAfterGame(TEST_USER_ID, false, 5, 50);

        assertThat(updatedStats.getTotalGames()).isEqualTo(11);
        assertThat(updatedStats.getWins()).isEqualTo(5);
        assertThat(updatedStats.getLosses()).isEqualTo(6);

        assertThat(updatedStats.getWinStreak()).isEqualTo(0);
        assertThat(updatedStats.getBestStreak()).isEqualTo(2);

        assertThat(updatedStats.getXp()).isEqualTo(50);
        assertThat(updatedStats.getLevel()).isEqualTo(1);

        assertThat(updatedStats.getTotalPlayTime()).isEqualTo(105);
    }

    // ====================================================================
    // 4. Test updateAfterGame - Edge Cases
    // ====================================================================

    @Test
    void updateAfterGame_whenStatsDoNotExist_thenThrowRuntimeException() {
        when(userStatsRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userStatsService.updateAfterGame(TEST_USER_ID, true, 1, 1);
        });

        verify(userStatsRepository, never()).save(any(UserStats.class));
    }

    @Test
    void updateAfterGame_xpCalculation_shouldCorrectlyChangeLevel() {
        when(userStatsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(initialStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStats statsLevel2 = userStatsService.updateAfterGame(TEST_USER_ID, true, 1, 300);
        assertThat(statsLevel2.getXp()).isEqualTo(300);
        assertThat(statsLevel2.getLevel()).isEqualTo(2);

        when(userStatsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(statsLevel2));

        UserStats statsLevel3 = userStatsService.updateAfterGame(TEST_USER_ID, true, 1, 400);
        assertThat(statsLevel3.getXp()).isEqualTo(700);
        assertThat(statsLevel3.getLevel()).isEqualTo(3);
    }
}