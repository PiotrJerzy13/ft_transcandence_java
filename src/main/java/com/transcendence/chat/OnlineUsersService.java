package com.transcendence.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineUsersService {

    private final SimpMessagingTemplate messagingTemplate;

    // Map of sessionId -> username
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    // Set of unique online usernames
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void addUser(String sessionId, String username) {
        log.info("Adding user: {} with session: {}", username, sessionId);

        sessionToUser.put(sessionId, username);
        onlineUsers.add(username);

        broadcastOnlineUsers();
    }

    public void removeUser(String sessionId) {
        String username = sessionToUser.remove(sessionId);

        if (username != null) {
            log.info("Removing user: {} with session: {}", username, sessionId);

            boolean hasOtherSessions = sessionToUser.containsValue(username);

            if (!hasOtherSessions) {
                onlineUsers.remove(username);
                log.info("User {} fully disconnected", username);
            } else {
                log.info("User {} still has other active sessions", username);
            }

            broadcastOnlineUsers();
        }
    }

    public List<String> getOnlineUsers() {
        return new ArrayList<>(onlineUsers);
    }

    private void broadcastOnlineUsers() {
        List<String> users = getOnlineUsers();
        log.info("Broadcasting online users: {}", users);
        messagingTemplate.convertAndSend("/topic/users", users);
    }

}
