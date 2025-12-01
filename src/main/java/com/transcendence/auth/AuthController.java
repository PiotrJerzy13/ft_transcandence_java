package com.transcendence.auth;

import com.transcendence.auth.dto.AuthResponse;
import com.transcendence.auth.dto.LoginRequest;
import com.transcendence.auth.dto.RegisterRequest;
import com.transcendence.entity.User;
import com.transcendence.security.jwt.JwtTokenProvider;
import com.transcendence.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public AuthController(AuthService authService,
                          JwtTokenProvider tokenProvider,
                          UserRepository userRepository) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // 1. Register user (hashes password, creates stats)
            User user = authService.registerUser(registerRequest);

            // 2. Create the Authentication object manually for token generation
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null
            );

            // 3. Generate JWT
            String token = tokenProvider.generateToken(authentication);

            // 4. Return the required AuthResponse (201 Created)
            AuthResponse response = new AuthResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    token
            );

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. Authenticate user via AuthService
        String token = authService.authenticateUser(loginRequest);

        // 2. Fetch the User entity to populate the response DTO
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after successful authentication."));

        // 3. Return the AuthResponse (200 OK)
        AuthResponse response = new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                token
        );

        return ResponseEntity.ok(response);
    }
}