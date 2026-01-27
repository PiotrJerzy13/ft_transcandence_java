package com.transcendence.security.details;

import com.transcendence.user.UserRepository;
import com.transcendence.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private final String MOCK_USERNAME = "testuser";
    private final String MOCK_PASSWORD_HASH = "mock_hash";
    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User(MOCK_USERNAME, "t@example.com", MOCK_PASSWORD_HASH, null, "active");
        mockUser.setId(1L);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Arrange
        when(userRepository.findByUsername(MOCK_USERNAME)).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(MOCK_USERNAME);

        // Assert
        assertNotNull(userDetails);
        assertEquals(MOCK_USERNAME, userDetails.getUsername());
        assertEquals(MOCK_PASSWORD_HASH, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
        verify(userRepository, times(1)).findByUsername(MOCK_USERNAME);
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername(MOCK_USERNAME)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(MOCK_USERNAME);
        });

        verify(userRepository, times(1)).findByUsername(MOCK_USERNAME);
    }
}