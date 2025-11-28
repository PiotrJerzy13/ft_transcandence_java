package com.transcendence.user;

import com.transcendence.entity.User;
import com.transcendence.entity.UserStats;
import com.transcendence.stats.dto.UserStatsDto;
import com.transcendence.user.dto.AchievementsResponse;
import com.transcendence.user.dto.UserMeResponse;
import com.transcendence.user.dto.UserProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication; // NEW IMPORT
import org.springframework.security.core.context.SecurityContextHolder; // NEW IMPORT
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    // constructor injection
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // The principal is typically the UserDetails object we created in CustomUserDetailsService
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Look up the actual User ID (Long) from the repository
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."))
                .getId();
    }

    // GET /api/user/me
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMe() {
        Long userId = getAuthenticatedUserId();

        User user = userService.getUserById(userId);

        return ResponseEntity.ok(new UserMeResponse(
                user.getId(),
                user.getUsername()
        ));
    }

    // GET /api/user/profile
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        Long userId = getAuthenticatedUserId();

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
