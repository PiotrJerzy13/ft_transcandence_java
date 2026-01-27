package com.transcendence.security.websocket;

import com.transcendence.auth.BlacklistedTokenRepository;
import com.transcendence.security.jwt.JwtTokenProvider;
import org.springframework.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final BlacklistedTokenRepository blacklistRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            log.warn("StompHeaderAccessor is null!");
            return message;
        }

        log.info("=== WebSocket Message Intercepted ===");
        log.info("Command: {}", accessor.getCommand());
        log.info("Session ID: {}", accessor.getSessionId());
        log.info("Current User: {}", accessor.getUser());
        log.info("Session Attributes: {}", accessor.getSessionAttributes());

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("Processing CONNECT frame");
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Authorization header present: {}", authHeader != null);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (tokenProvider.validateToken(token) && !blacklistRepository.existsByToken(token)) {
                    String username = tokenProvider.getUsernameFromToken(token);
                    log.info("✓ Authentication successful for user: {}", username);

                    Principal principal = () -> username;
                    accessor.setUser(principal);

                    if (accessor.getSessionAttributes() != null) {
                        accessor.getSessionAttributes().put("username", username);
                        log.info("✓ Stored username in session attributes");
                    } else {
                        log.error("✗ Session attributes is NULL!");
                    }
                } else {
                    log.error("✗ Token validation failed or token is blacklisted");
                    throw new AccessDeniedException("Invalid or blacklisted token");
                }
            } else {
                log.error("✗ No valid Authorization header");
                throw new AccessDeniedException("No authorization header found");
            }
        } else {
            // For all other commands
            log.info("Processing {} frame", accessor.getCommand());

            if (accessor.getUser() == null) {
                log.warn("Principal is null, attempting to restore from session");

                if (accessor.getSessionAttributes() != null) {
                    String username = (String) accessor.getSessionAttributes().get("username");
                    log.info("Username from session: {}", username);

                    if (username != null) {
                        Principal principal = () -> username;
                        accessor.setUser(principal);
                        log.info("✓ Restored Principal for user: {}", username);
                    } else {
                        log.error("✗ No username found in session attributes!");
                        log.error("Available session keys: {}", accessor.getSessionAttributes().keySet());
                    }
                } else {
                    log.error("✗ Session attributes is NULL for non-CONNECT frame!");
                }
            } else {
                log.info("✓ Principal already set: {}", accessor.getUser().getName());
            }
        }

        log.info("=== End Intercept (User: {}) ===", accessor.getUser());
        return message;
    }
}