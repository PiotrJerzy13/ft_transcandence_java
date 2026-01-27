package com.transcendence.chat;

import com.transcendence.chat.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final OnlineUsersService onlineUsersService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO messageDTO, Principal principal) {
        String username = principal.getName();

        messageDTO.setSender(username);
        messageDTO.setTimestamp(LocalDateTime.now());

        log.info("Received message from {}: {}", username, messageDTO.getContent());

        // Broadcast message to all users
        messagingTemplate.convertAndSend("/topic/public", messageDTO);

        // Save message asynchronously
        chatService.saveMessage(messageDTO);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDTO messageDTO,
                        SimpMessageHeaderAccessor headerAccessor,
                        Principal principal) {
        String username = principal.getName();
        String sessionId = headerAccessor.getSessionId();

        log.info("User {} joined the chat (session: {})", username, sessionId);

        // Send chat history to the newly connected user
        List<ChatMessageDTO> recentMessages = chatService.getRecentMessages(50);
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/history",
                recentMessages
        );

        List<String> onlineUsers = onlineUsersService.getOnlineUsers();
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/users",
                onlineUsers
        );

        // Notify others that a user joined
        ChatMessageDTO joinMessage = new ChatMessageDTO();
        joinMessage.setSender("System");
        joinMessage.setContent(username + " joined the chat");
        joinMessage.setType("JOIN");
        joinMessage.setTimestamp(LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/public", joinMessage);
    }
}