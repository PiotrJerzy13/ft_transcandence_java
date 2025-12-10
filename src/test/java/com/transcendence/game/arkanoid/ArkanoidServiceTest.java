package com.transcendence.game.arkanoid;

import com.transcendence.entity.ArkanoidScore;
import com.transcendence.entity.User;
import com.transcendence.entity.UserStats;
import com.transcendence.game.arkanoid.dto.ArkanoidScoreRequest;
import com.transcendence.stats.UserStatsService;
import com.transcendence.stats.dto.SaveScoreResponse;
import com.transcendence.stats.dto.UserStatsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArkanoidServiceTest {

    @Mock
    private ArkanoidScoreRepository arkanoidScoreRepository;

    @Mock
    private UserStatsService userStatsService;

    @InjectMocks
    private ArkanoidService arkanoidService;

    private static final Long USER_ID = 1L;
    private static final int XP_EARNED = 100;
    private static final int SCORE = 5000;
    private static final int LEVEL_REACHED = 5;
    private static final int DURATION = 120; // seconds

    private ArkanoidScoreRequest mockRequest;
    private UserStats mockUpdatedUserStats;
    private User mockUser;

    @BeforeEach
    void setUp() {

        mockUser = new User();
        mockUser.setId(USER_ID);

        mockRequest = new ArkanoidScoreRequest();
        mockRequest.setScore(SCORE);
        mockRequest.setLevelReached(LEVEL_REACHED);
        mockRequest.setXpEarned(XP_EARNED);
        mockRequest.setDuration(DURATION);
        mockRequest.setBlocksDestroyed(150);
        mockRequest.setPowerUpsCollected(10);

        mockUpdatedUserStats = UserStats.builder()
                .id(1L)
                .user(mockUser)
                .level(3)
                .xp(200)
                .rank(UserStats.Rank.Novice)

                .build();
    }

    @Test
    void saveScore_shouldSaveScoreAndUpdateUserStats_andReturnCorrectResponse() {

        when(arkanoidScoreRepository.save(any(ArkanoidScore.class)))
                .thenAnswer(invocation -> {
                    ArkanoidScore savedScore = invocation.getArgument(0);
                    savedScore.setId(99L);
                    return savedScore;
                });

        when(userStatsService.updateAfterGame(
                eq(USER_ID),
                eq(true),
                eq(DURATION),
                eq(XP_EARNED)
        )).thenReturn(mockUpdatedUserStats);

        SaveScoreResponse response = arkanoidService.saveScore(USER_ID, mockRequest);

        ArgumentCaptor<ArkanoidScore> scoreCaptor = ArgumentCaptor.forClass(ArkanoidScore.class);
        verify(arkanoidScoreRepository, times(1)).save(scoreCaptor.capture());

        ArkanoidScore capturedScore = scoreCaptor.getValue();
        assertEquals(USER_ID, capturedScore.getUserId());
        assertEquals(SCORE, capturedScore.getScore());
        assertEquals(XP_EARNED, capturedScore.getXpEarned());
        assertEquals(DURATION, capturedScore.getDuration());

        verify(userStatsService, times(1)).updateAfterGame(
                USER_ID, true, DURATION, XP_EARNED
        );

        assertEquals("Score saved successfully", response.getMessage());

        // Check the UserStatsDto in the response
        UserStatsDto responseStats = response.getUserStats();
        assertNotNull(responseStats);
        assertEquals(mockUpdatedUserStats.getLevel(), responseStats.getLevel());
        assertEquals(mockUpdatedUserStats.getXp(), responseStats.getXp()); // Using 'getXp' instead of 'getTotalXp'
    }

}