package com.transcendence.security.jwt;

import com.transcendence.security.details.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private final String VALID_TOKEN = "mock.valid.token";
    private final String MOCK_USERNAME = "testuser";
    private final UserDetails mockUserDetails = new User(
            MOCK_USERNAME, "password", Collections.emptyList());

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldAuthenticate_whenValidTokenProvided() throws Exception {
        String header = "Bearer " + VALID_TOKEN;
        when(request.getHeader("Authorization")).thenReturn(header);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(VALID_TOKEN)).thenReturn(MOCK_USERNAME);
        when(customUserDetailsService.loadUserByUsername(MOCK_USERNAME)).thenReturn(mockUserDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertEquals(MOCK_USERNAME, ((UserDetails) authentication.getPrincipal()).getUsername());

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenInvalidTokenProvided() throws Exception {

        String header = "Bearer " + VALID_TOKEN;
        when(request.getHeader("Authorization")).thenReturn(header);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(false); // Invalid token

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain, times(1)).doFilter(request, response);
        verify(tokenProvider, never()).getUsernameFromToken(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenNoTokenProvided() throws Exception {

        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());


        verify(filterChain, times(1)).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(anyString());
    }
}