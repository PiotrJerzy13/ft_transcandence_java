package com.transcendence.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_JWT_SECRET = "dGVzdFNlY3JldEtleUZvckpXVFRva2VuVGVzdGluZ1B1cnBvc2VzT25seVRoaXNJc0F0TGVhc3QyNTZCaXRz";
    private static final long TEST_JWT_EXPIRATION = 3600000; // 1 hour

    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setup() {
        jwtTokenProvider = new JwtTokenProvider();

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", TEST_JWT_EXPIRATION);
    }

    // ====================================================================
    // 1. Test generateToken - with UserDetails principal
    // ====================================================================

    @Test
    void generateToken_withUserDetailsPrincipal_shouldReturnValidToken() {
        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // Verify token structure (JWT format: header.payload.signature)
        assertThat(token.split("\\.")).hasSize(3);

        // Verify the token is valid
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();

        // Verify username can be extracted
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertThat(extractedUsername).isEqualTo(TEST_USERNAME);
    }

    // ====================================================================
    // 2. Test generateToken - with String principal
    // ====================================================================

    @Test
    void generateToken_withStringPrincipal_shouldReturnValidToken() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USERNAME, null);

        String token = jwtTokenProvider.generateToken(authentication);


        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertThat(extractedUsername).isEqualTo(TEST_USERNAME);
    }

    // ====================================================================
    // 3. Test generateToken - with unsupported principal type
    // ====================================================================

    @Test
    void generateToken_withUnsupportedPrincipal_shouldThrowException() {

        Object unsupportedPrincipal = new Object(); // Not UserDetails or String
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                unsupportedPrincipal, null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> jwtTokenProvider.generateToken(authentication)
        );

        assertThat(exception.getMessage()).contains("Unsupported principal type");
    }

    // ====================================================================
    // 5. Test getUsernameFromToken
    // ====================================================================

    @Test
    void getUsernameFromToken_withValidToken_shouldReturnUsername() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USERNAME, null);
        String token = jwtTokenProvider.generateToken(authentication);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(extractedUsername).isEqualTo(TEST_USERNAME);
    }

    @Test
    void getUsernameFromToken_withInvalidToken_shouldThrowException() {

        String invalidToken = "invalid.jwt.token";

        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        });
    }

    // ====================================================================
    // 6. Test validateToken - valid token
    // ====================================================================

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        // ARRANGE
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USERNAME, null);
        String token = jwtTokenProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    // ====================================================================
    // 7. Test validateToken - invalid tokens
    // ====================================================================

    @Test
    void validateToken_withMalformedToken_shouldReturnFalse() {

        String malformedToken = "this.is.not.a.valid.jwt";

        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withEmptyToken_shouldReturnFalse() {

        String emptyToken = "";

        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withNullToken_shouldReturnFalse() {

        boolean isValid = jwtTokenProvider.validateToken(null);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnFalse() {

        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtSecret", TEST_JWT_SECRET);
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtExpirationInMs", -1000L); // Already expired

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USERNAME, null);
        String expiredToken = shortExpirationProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withWrongSignature_shouldReturnFalse() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USERNAME, null);
        String token = jwtTokenProvider.generateToken(authentication);

        JwtTokenProvider differentSecretProvider = new JwtTokenProvider();
        String differentSecret = "ZGlmZmVyZW50U2VjcmV0S2V5Rm9ySlRXVG9rZW5UZXN0aW5nUHVycG9zZXNPbmx5VGhpc0lzQXRMZWFzdDI1NkJpdHM=";
        ReflectionTestUtils.setField(differentSecretProvider, "jwtSecret", differentSecret);
        ReflectionTestUtils.setField(differentSecretProvider, "jwtExpirationInMs", TEST_JWT_EXPIRATION);

        boolean isValid = differentSecretProvider.validateToken(token);

        assertThat(isValid).isFalse();
    }

}