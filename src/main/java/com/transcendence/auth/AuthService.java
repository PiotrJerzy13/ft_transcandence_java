package com.transcendence.auth;

import com.transcendence.auth.dto.RegisterRequest;
import com.transcendence.entity.User;
import com.transcendence.user.UserRepository;
import com.transcendence.stats.UserStatsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transcendence.auth.dto.LoginRequest;
import com.transcendence.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserStatsService userStatsService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserStatsService userStatsService,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userStatsService = userStatsService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    public String authenticateUser(LoginRequest loginRequest) {
        // Use the AuthenticationManager to verify credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }

    /**
     * Handles user registration, hashing the password and creating initial stats.
     */
    @Transactional
    public User registerUser(RegisterRequest request) {
        // Check for existing user (for 409 Conflict)
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use.");
        }

        // 1. Create User entity
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .avatarUrl("default.png") // Default value
                .status("online")        // Default value
                .build();

        // 2. Save User
        User savedUser = userRepository.save(newUser);

        // 3. Create initial stats using the service you provided
        userStatsService.createInitialStats(savedUser);

        return savedUser;
    }
}