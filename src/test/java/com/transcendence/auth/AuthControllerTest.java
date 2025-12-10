package com.transcendence.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transcendence.auth.dto.LoginRequest;
import com.transcendence.auth.dto.RegisterRequest;
import com.transcendence.config.SecurityConfig;
import com.transcendence.entity.User;
import com.transcendence.security.details.CustomUserDetailsService;
import com.transcendence.security.jwt.JwtAuthenticationFilter;
import com.transcendence.security.jwt.JwtTokenProvider;
import com.transcendence.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private RegisterRequest mockRegisterRequest;
    private LoginRequest mockLoginRequest;
    private User mockUser;
    private final String MOCK_TOKEN = "mock.jwt.token";

    @BeforeEach
    void setup() throws Exception {

        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any(FilterChain.class)
        );

        mockRegisterRequest = new RegisterRequest("new_user", "new@example.com", "securePass");
        mockLoginRequest = new LoginRequest("existing_user", "securePass");

        mockUser = new User("test_user", "t@example.com", "hash", null, "active");
        mockUser.setId(1L);
    }

    @Test
    void registerUser_shouldReturn201CreatedAndAuthResponse_whenSuccessful() throws Exception {
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(mockUser);
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn(MOCK_TOKEN);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mockUser.getId()))
                .andExpect(jsonPath("$.username").value(mockUser.getUsername()))
                .andExpect(jsonPath("$.email").value(mockUser.getEmail()))
                .andExpect(jsonPath("$.token").value(MOCK_TOKEN));
    }

    @Test
    void registerUser_shouldReturn409Conflict_whenAuthServiceThrowsRuntimeException() throws Exception {
        when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username already taken."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockRegisterRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Username already taken."));
    }

    @Test
    void authenticateUser_shouldReturn200OkAndAuthResponse_whenCredentialsAreValid() throws Exception {
        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(MOCK_TOKEN);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockUser.getId()))
                .andExpect(jsonPath("$.username").value(mockUser.getUsername()))
                .andExpect(jsonPath("$.email").value(mockUser.getEmail()))
                .andExpect(jsonPath("$.token").value(MOCK_TOKEN));
    }
}