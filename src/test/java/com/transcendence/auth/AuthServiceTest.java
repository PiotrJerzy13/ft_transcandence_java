package com.transcendence.auth;

import com.transcendence.auth.dto.LoginRequest;
import com.transcendence.auth.dto.RegisterRequest;
import com.transcendence.entity.User;
import com.transcendence.security.jwt.JwtTokenProvider;
import com.transcendence.stats.UserStatsService;
import com.transcendence.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserStatsService userStatsService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private Authentication mockAuthentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setup() {
        registerRequest = new RegisterRequest("test_user", "t@example.com", "password123");
        loginRequest = new LoginRequest("test_user", "password123");

        mockUser = new User("test_user", "t@example.com", "hash", null, "active");
    }

    // ====================================================================
    // 1. Test registerUser
    // ====================================================================

    @Test
    void registerUser_shouldCreateUserAndInitialStats_whenValid() {
        // ARRANGE
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        // Ensure that saving returns the user object with its new ID
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User resultUser = authService.registerUser(registerRequest);

        assertThat(resultUser).isEqualTo(mockUser);

        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userStatsService, times(1)).createInitialStats(mockUser);
    }

    @Test
    void registerUser_shouldThrowException_whenUsernameExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            authService.registerUser(registerRequest);
        });

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowException_whenEmailExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            authService.registerUser(registerRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    // ====================================================================
    // 2. Test authenticateUser
    // ====================================================================

    @Test
    void authenticateUser_shouldReturnJwtToken_whenCredentialsAreValid() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        when(tokenProvider.generateToken(mockAuthentication)).thenReturn("mock_jwt_token");

        String token = authService.authenticateUser(loginRequest);

        assertThat(token).isEqualTo("mock_jwt_token");

        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken("test_user", "password123")
        );
    }

    @Test
    void authenticateUser_shouldThrowException_whenCredentialsAreInvalid() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        verify(tokenProvider, never()).generateToken(any());
    }
}