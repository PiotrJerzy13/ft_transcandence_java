package com.transcendence.game.arkanoid;

import com.transcendence.entity.User;
import com.transcendence.game.arkanoid.dto.ArkanoidHistoryResponse;
import com.transcendence.game.arkanoid.dto.ArkanoidScoreRequest;
import com.transcendence.stats.dto.SaveScoreResponse;
import com.transcendence.stats.dto.UserStatsDto;
import com.transcendence.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.transcendence.security.jwt.JwtTokenProvider;
import com.transcendence.security.details.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArkanoidController.class)
class ArkanoidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArkanoidService arkanoidService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "test_user";
    private static final String TEST_EMAIL = "test@example.com";

    private User mockUser;
    private ArkanoidScoreRequest scoreRequest;
    private SaveScoreResponse saveScoreResponse;
    private ArkanoidHistoryResponse historyResponse;

    @BeforeEach
    void setup() {
        // Setup User Entity
        mockUser = new User(TEST_USERNAME, TEST_EMAIL, "hash", null, "active");
        mockUser.setId(TEST_USER_ID);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        scoreRequest = new ArkanoidScoreRequest();
        scoreRequest.setScore(5000);
        scoreRequest.setLevelReached(5);
        scoreRequest.setXpEarned(100);
        scoreRequest.setDuration(300);
        scoreRequest.setBlocksDestroyed(150);
        scoreRequest.setPowerUpsCollected(10);

        UserStatsDto statsDto = UserStatsDto.builder()
                .totalGames(15)
                .wins(8)
                .losses(7)
                .winStreak(2)
                .bestStreak(3)
                .totalPlayTime(200)
                .rank("Intermediate")
                .level(3)
                .xp(250)
                .build();

        saveScoreResponse = SaveScoreResponse.builder()
                .message("Score saved successfully")
                .userStats(statsDto)
                .build();

        historyResponse = new ArkanoidHistoryResponse();
        historyResponse.setHistory(new ArrayList<>());
    }

    // ====================================================================
    // 1. Test POST /api/arkanoid/score
    // ====================================================================

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void saveScore_shouldReturnSaveScoreResponse_whenValidRequest() throws Exception {

        when(arkanoidService.saveScore(eq(TEST_USER_ID), any(ArkanoidScoreRequest.class)))
                .thenReturn(saveScoreResponse);

        mockMvc.perform(post("/api/arkanoid/score")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoreRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Score saved successfully"))
                .andExpect(jsonPath("$.userStats.total_games").value(15))
                .andExpect(jsonPath("$.userStats.wins").value(8))
                .andExpect(jsonPath("$.userStats.level").value(3))
                .andExpect(jsonPath("$.userStats.xp").value(250))
                .andExpect(jsonPath("$.userStats.rank").value("Intermediate"));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void saveScore_shouldHandleMinimalRequest() throws Exception {
        // ARRANGE
        ArkanoidScoreRequest minimalRequest = new ArkanoidScoreRequest();
        minimalRequest.setScore(1000);
        minimalRequest.setLevelReached(1);

        when(arkanoidService.saveScore(eq(TEST_USER_ID), any(ArkanoidScoreRequest.class)))
                .thenReturn(saveScoreResponse);

        mockMvc.perform(post("/api/arkanoid/score")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Score saved successfully"));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void saveScore_shouldHandleHighScore() throws Exception {
        // ARRANGE
        ArkanoidScoreRequest highScoreRequest = new ArkanoidScoreRequest();
        highScoreRequest.setScore(50000);
        highScoreRequest.setLevelReached(20);
        highScoreRequest.setXpEarned(500);
        highScoreRequest.setDuration(1800);
        highScoreRequest.setBlocksDestroyed(2000);
        highScoreRequest.setPowerUpsCollected(50);

        when(arkanoidService.saveScore(eq(TEST_USER_ID), any(ArkanoidScoreRequest.class)))
                .thenReturn(saveScoreResponse);

        mockMvc.perform(post("/api/arkanoid/score")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(highScoreRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Score saved successfully"));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void saveScore_shouldHandleNullOptionalFields() throws Exception {
        ArkanoidScoreRequest requestWithNulls = new ArkanoidScoreRequest();
        requestWithNulls.setScore(2000);
        requestWithNulls.setLevelReached(3);

        when(arkanoidService.saveScore(eq(TEST_USER_ID), any(ArkanoidScoreRequest.class)))
                .thenReturn(saveScoreResponse);

        mockMvc.perform(post("/api/arkanoid/score")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithNulls)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Score saved successfully"));
    }

    // ====================================================================
    // 2. Test GET /api/arkanoid/history
    // ====================================================================

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void getHistory_shouldReturnArkanoidHistoryResponse_whenAuthenticated() throws Exception {
        when(arkanoidService.getHistory(TEST_USER_ID)).thenReturn(historyResponse);

        mockMvc.perform(get("/api/arkanoid/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").isArray())
                .andExpect(jsonPath("$.history").isEmpty());
    }

    @Test
    void getHistory_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(get("/api/arkanoid/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void getHistory_shouldReturnHistoryWithScores_whenScoresExist() throws Exception {

        when(arkanoidService.getHistory(TEST_USER_ID)).thenReturn(historyResponse);

        mockMvc.perform(get("/api/arkanoid/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").isArray());
    }

}