package com.transcendence.security.websocket;

import com.transcendence.auth.BlacklistedTokenRepository;
import com.transcendence.security.jwt.JwtTokenProvider;
import org.springframework.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final BlacklistedTokenRepository blacklistRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // Only intercept the CONNECT frame
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // 1. Validate JWT signature and expiration
                // 2. Check if token is in the SQLite blacklist
                if (tokenProvider.validateToken(token) && !blacklistRepository.existsByToken(token)) {
                    String username = tokenProvider.getUsernameFromToken(token);

                    // Set the user identity for this WebSocket session
                    accessor.setUser(() -> username);
                } else {
                    throw new AccessDeniedException("Invalid or blacklisted token");
                }
            } else {
                throw new AccessDeniedException("No authorization header found");
            }
        }
        return message;
    }
}
