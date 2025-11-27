package com.transcendence.user;

import com.transcendence.entity.User;
import com.transcendence.entity.UserStats;
import com.transcendence.stats.dto.UserStatsDto;
import com.transcendence.user.dto.AchievementsResponse;
import com.transcendence.user.dto.UserMeResponse;
import com.transcendence.user.dto.UserProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    // constructor injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/user/me
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMe() {
        Long userId = 1L; // TODO: replace with JWT

        User user = userService.getUserById(userId);

        return ResponseEntity.ok(new UserMeResponse(
                user.getId(),
                user.getUsername()
        ));
    }

    // GET /api/user/profile
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        Long userId = 1L; // TODO: replace with JWT

        User user = userService.getUserById(userId);
        UserStats stats = userService.getStatsByUserId(userId);

        return ResponseEntity.ok(new UserProfileResponse(
                new UserProfileResponse.UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail()
                ),
                UserStatsDto.fromEntity(stats),
                new Object[]{} // empty achievements
        ));
    }

    // GET /api/user/achievements
    @GetMapping("/achievements")
    public ResponseEntity<AchievementsResponse> getAchievements() {
        return ResponseEntity.ok(new AchievementsResponse(new Object[]{}));
    }
}
