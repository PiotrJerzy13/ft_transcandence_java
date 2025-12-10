package com.transcendence.leaderboard;

import com.transcendence.leaderboard.dto.LeaderboardPlayerDTO;
import com.transcendence.leaderboard.dto.LeaderboardResponseDTO;
import com.transcendence.security.details.CustomUserDetailsService;
import com.transcendence.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.transcendence.user.UserRepository;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaderboardController.class)
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaderboardService leaderboardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private final String LEADERBOARD_URL = "/api/leaderboard";

    private LeaderboardResponseDTO createMockResponse() {
        LeaderboardPlayerDTO player = LeaderboardPlayerDTO.builder()
                .id(1L).username("test_user").level(10).rank("ELITE").totalGames(50).winRate(75.0).xp(1000L)
                .build();

        return LeaderboardResponseDTO.builder()
                .leaderboard(List.of(player))
                .build();
    }

    // ====================================================================
    // 1. Test GET /api/leaderboard - Happy Path (Success)
    // ====================================================================

    @Test
    @WithMockUser(username = "test_user")
    void getLeaderboard_shouldReturnOkAndData_whenServiceSucceeds() throws Exception {
        LeaderboardResponseDTO mockResponse = createMockResponse();
        when(leaderboardService.getLeaderboard()).thenReturn(mockResponse);

        mockMvc.perform(get(LEADERBOARD_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaderboard").isArray())
                .andExpect(jsonPath("$.leaderboard[0].username").value("test_user"))
                .andExpect(jsonPath("$.leaderboard[0].winRate").value(75.0));
    }

    // ====================================================================
    // 2. Test GET /api/leaderboard - Error Path (500)
    // ====================================================================

    @Test
    @WithMockUser(username = "test_user")
    void getLeaderboard_shouldReturnInternalServerError_whenServiceThrowsException() throws Exception {

        when(leaderboardService.getLeaderboard()).thenThrow(new RuntimeException("Database down"));

        mockMvc.perform(get(LEADERBOARD_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.leaderboard").doesNotExist())
                .andExpect(jsonPath("$.error").value("Failed to load leaderboard"));
    }
}