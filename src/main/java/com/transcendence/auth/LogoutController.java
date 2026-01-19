package com.transcendence.auth;


import com.transcendence.entity.BlacklistedToken;
import com.transcendence.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class LogoutController {

    private final BlacklistedTokenRepository blacklistRepository;
    private final JwtTokenProvider tokenProvider;

    public LogoutController(BlacklistedTokenRepository blacklistRepository,
                            JwtTokenProvider tokenProvider) {
        this.blacklistRepository = blacklistRepository;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (!tokenProvider.validateToken(token)) {
                return ResponseEntity.badRequest().body("Invalid or expired token");
            }

            try {
                LocalDateTime expiry = tokenProvider.getExpiryDateFromToken(token);

                BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                        .token(token)
                        .expiryDate(expiry)
                        .build();

                blacklistRepository.save(blacklistedToken);
                return ResponseEntity.ok("Logged out successfully");

            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error processing token");
            }
        }
        return ResponseEntity.badRequest().body("No valid token found");
    }
}
