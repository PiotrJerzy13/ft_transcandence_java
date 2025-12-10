package com.transcendence.user;

import com.transcendence.entity.User;
import com.transcendence.entity.UserStats;
import com.transcendence.stats.UserStatsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatsRepository userStatsRepository;

    @InjectMocks
    private UserService userService;

    private static final Long TEST_USER_ID = 1L;
    private final User mockUser = new User("test_user", "t@example.com", "hash", null, "active");
    private final UserStats mockStats = UserStats.builder()
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

    @Test
    void getUserById_whenUserExists_thenReturnUser() {
        when(userRepository.findById(TEST_USER_ID))
                .thenReturn(Optional.of(mockUser));

        User resultUser = userService.getUserById(TEST_USER_ID);

        assertThat(resultUser).isEqualTo(mockUser);
    }

    @Test
    void getUserById_whenUserDoesNotExist_thenThrowRuntimeException() {
        when(userRepository.findById(TEST_USER_ID))
                .thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            userService.getUserById(TEST_USER_ID);
        });
    }

    // --- Test Cases for getStatsByUserId ---

    @Test
    void getStatsByUserId_whenStatsExist_thenReturnStats() {
        when(userStatsRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(mockStats));

        UserStats resultStats = userService.getStatsByUserId(TEST_USER_ID);

        assertThat(resultStats).isEqualTo(mockStats);
    }

    @Test
    void getStatsByUserId_whenStatsDoNotExist_thenThrowRuntimeException() {
        when(userStatsRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.getStatsByUserId(TEST_USER_ID);
        });
    }
}
