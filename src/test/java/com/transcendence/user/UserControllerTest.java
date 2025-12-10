package com.transcendence.user;

import com.transcendence.entity.User;
import com.transcendence.entity.UserStats;
import com.transcendence.security.details.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.transcendence.security.jwt.JwtTokenProvider;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // --- Test Data Setup ---
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "test_user";
    private static final String TEST_EMAIL = "t@example.com";

    private User mockUser;
    private UserStats mockStats;

    @BeforeEach
    void setup() {
        mockUser = new User(TEST_USERNAME, TEST_EMAIL, "hash", null, "active");
        mockUser.setId(TEST_USER_ID);

        mockStats = UserStats.builder()
                .id(TEST_USER_ID)
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

        // return the mock user.
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
    }

    // ====================================================================
    // 1. Test GET /api/user/me
    // ====================================================================

    @Test
    // Simulates an authenticated user with the name TEST_USERNAME
    @WithMockUser(username = TEST_USERNAME)
    void getMe_shouldReturnUserMeResponse_whenAuthenticated() throws Exception {
        when(userService.getUserById(TEST_USER_ID)).thenReturn(mockUser);

        // DTO Construction: UserMeResponse only contains ID and Username
        mockMvc.perform(get("/api/user/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));
    }

    @Test
    void getMe_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized()); // Expect 401 Unauthorized
    }


    // ====================================================================
    // 2. Test GET /api/user/profile
    // ====================================================================

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void getProfile_shouldReturnUserProfileResponse_whenAuthenticated() throws Exception {

        when(userService.getUserById(TEST_USER_ID)).thenReturn(mockUser);
        when(userService.getStatsByUserId(TEST_USER_ID)).thenReturn(mockStats);

        mockMvc.perform(get("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Assert UserDto part
                .andExpect(jsonPath("$.user.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.user.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.user.email").value(TEST_EMAIL))
                // Assert UserStatsDto part
                .andExpect(jsonPath("$.stats.total_games").value(mockStats.getTotalGames()))
                .andExpect(jsonPath("$.stats.level").value(mockStats.getLevel()))
                .andExpect(jsonPath("$.stats.xp").value(mockStats.getXp()))
                // Assert Achievements part (empty array)
                .andExpect(jsonPath("$.achievements").isArray())
                .andExpect(jsonPath("$.achievements").isEmpty());
    }

    // ====================================================================
    // 3. Test GET /api/user/achievements
    // ====================================================================

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void getAchievements_shouldReturnEmptyArray() throws Exception {

        mockMvc.perform(get("/api/user/achievements")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.achievements").isArray())
                .andExpect(jsonPath("$.achievements").isEmpty());
    }
}