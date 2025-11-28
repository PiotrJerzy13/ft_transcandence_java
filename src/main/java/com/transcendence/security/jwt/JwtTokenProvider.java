package com.transcendence.security.jwt;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Inject the secret key and expiration time from application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    // Get the signing key from the secret string
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // --- 1. Generate Token ---
    public String generateToken(Authentication authentication) {
        // Get the principal (user details) from the authenticated object
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // Build the JWT
        return Jwts.builder()
                .setSubject(username) // The unique identifier for the token (the username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS512) // Sign with the secret key
                .compact();
    }

    // --- 2. Get Username from Token ---
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // --- 3. Validate Token ---
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            // Log this: Invalid JWT token
        } catch (ExpiredJwtException ex) {
            // Log this: Expired JWT token
        } catch (UnsupportedJwtException ex) {
            // Log this: Unsupported JWT token
        } catch (IllegalArgumentException ex) {
            // Log this: JWT claims string is empty
        }
        return false;
    }
}