package com.transcendence.auth;

import com.transcendence.entity.User;
import com.transcendence.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "https://localhost:5173"}, allowCredentials = "true")
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            User user = userRepository.findByUsername(request.username())
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(request.username());
                        newUser.setEmail(request.username() + "@test.com");
                        newUser.setPasswordHash("dummy");
                        newUser.setStatus("online");

                        String now = java.time.LocalDateTime.now().toString();
                        newUser.setCreatedAt(now);
                        newUser.setUpdatedAt(now);

                        return userRepository.save(newUser);
                    });

            response.addCookie(createCookie("userId", user.getId().toString()));
            response.addCookie(createCookie("username", user.getUsername()));

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "user", Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "status", user.getStatus()
                    )
            ));
        } catch (Exception e) {
            e.printStackTrace();  // Log to console
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Login failed: " + e.getMessage()
            ));
        }
    }

    private jakarta.servlet.http.Cookie createCookie(String name, String value) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24 hours
        return cookie;
    }
}

// DTO for login request
record LoginRequest(String username, String password) {}