package com.transcendence.chat;

import com.transcendence.chat.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
//    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO message, Principal principal) {
        log.info("Controller received - Principal: {}", principal);

        if (principal == null) {
            log.error("Principal is NULL in controller!");
            throw new IllegalStateException("User not authenticated");
        }

        message.setSender(principal.getName());
        message.setTimestamp(LocalDateTime.now());

        chatService.saveMessage(message);

        return message;
    }

    // This is triggered when a client subscribes to /queue/history
    @SubscribeMapping("/user/queue/history")
    public List<ChatMessageDTO> getHistory(Principal principal) {
        log.info("History requested by user: {}", principal != null ? principal.getName() : "anonymous");

        return chatService.getRecentMessages(50);
    }
}