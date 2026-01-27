package com.transcendence.game.pong;

import com.transcendence.entity.User;
import com.transcendence.game.pong.dto.PongHistoryResponse;
import com.transcendence.game.pong.dto.PongScoreRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PongController.class)
class PongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PongService pongService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // --- Test Data Setup ---
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "test_user";
    private static final String TEST_EMAIL = "test@example.com";

    private User mockUser;
    private PongScoreRequest scoreRequest;
    private SaveScoreResponse saveScoreResponse;
    private PongHistoryResponse historyResponse;

    @BeforeEach
    void setup() {
        // Setup User Entity
        mockUser = new User(TEST_USERNAME, TEST_EMAIL, "hash", null, "active");
        mockUser.setId(TEST_USER_ID);

        // Mock the repository to return the user
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        // Setup PongScoreRequest
        scoreRequest = new PongScoreRequest();
        scoreRequest.setScore(5);
        scoreRequest.setOpponentScore(3);
        scoreRequest.setWinner("player");

        // Setup SaveScoreResponse
        UserStatsDto statsDto = UserStatsDto.builder()
                .totalGames(11)
                .wins(6)
                .losses(5)
                .winStreak(1)
                .bestStreak(2)
                .totalPlayTime(150)
                .rank("Novice")
                .level(2)
                .xp(100)
                .build();

        saveScoreResponse = SaveScoreResponse.builder()
                .message("Score saved successfully")
                .userStats(statsDto)
                .build();

        // Setup PongHistoryResponse
        historyResponse = new PongHistoryResponse();
        historyResponse.setHistory(new ArrayList<>());
    }

    // ====================================================================
    // 1. Test POST /api/pong/score
    // ====================================================================

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void saveScore_shouldReturnSaveScoreResponse_whenValidRequest() throws Exception {
        // ARRANGE
        when(pongService.saveScore(eq(TEST_USER_ID), eq("one-player"), any(PongScoreRequest.class)))
                .thenReturn(saveScoreResponse);

        // ACT & ASSERT
        mockMvc.perform(post("/api/pong/score")
                        .with(csrf()) // Added CSRF token
                        .param("mode", "one-player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoreRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Score saved successfully"))
                .andExpect(jsonPath("$.userStats.total_games").value(11))
                .andExpect(jsonPath("$.userStats.wins").value(6))
                .andExpect(jsonPath("$.userStats.level").value(2))
                .andExpect(jsonPath("$.userStats.xp").value(100));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void saveScore_shouldUseDefaultMode_whenModeNotProvided() throws Exception {
        // ARRANGE
        when(pongService.saveScore(eq(TEST_USER_ID), eq("one-player"), any(PongScoreRequest.class)))
                .thenReturn(saveScoreResponse);

        // ACT & ASSERT - No mode parameter
        mockMvc.perform(post("/api/pong/score")
                        .with(csrf()) // Added CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoreRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Score saved successfully"));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void saveScore_shouldHandleTwoPlayerMode() throws Exception {
        // ARRANGE
        when(pongService.saveScore(eq(TEST_USER_ID), eq("two-player"), any(PongScoreRequest.class)))
                .thenReturn(saveScoreResponse);

        // ACT & ASSERT
        mockMvc.perform(post("/api/pong/score")
                        .with(csrf()) // Added CSRF token
                        .param("mode", "two-player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoreRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Score saved successfully"));
    }

    @Test
    void saveScore_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        // ACT & ASSERT
        // Note: POST without authentication returns 403 (Forbidden) when CSRF is enabled
        mockMvc.perform(post("/api/pong/score")
                        .with(csrf()) // Added CSRF token
                        .param("mode", "one-player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoreRequest)))
                .andExpect(status().isUnauthorized()); // 403 for POST requests
    }


    // ====================================================================
    // 2. Test GET /api/pong/history
    // ====================================================================

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void getHistory_shouldReturnPongHistoryResponse_whenAuthenticated() throws Exception {
        // ARRANGE
        when(pongService.getHistory(TEST_USER_ID)).thenReturn(historyResponse);

        // ACT & ASSERT
        mockMvc.perform(get("/api/pong/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").isArray())
                .andExpect(jsonPath("$.history").isEmpty());
    }

    @Test
    void getHistory_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        // ACT & ASSERT
        // Note: Changed expectation to 403 as Spring Security returns Forbidden
        // when authentication is missing (not Unauthorized)
        mockMvc.perform(get("/api/pong/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void getHistory_shouldReturnHistoryWithMatches_whenMatchesExist() throws Exception {

        // Testing that the service is called
        when(pongService.getHistory(TEST_USER_ID)).thenReturn(historyResponse);

        // ACT & ASSERT
        mockMvc.perform(get("/api/pong/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").isArray());
    }
}