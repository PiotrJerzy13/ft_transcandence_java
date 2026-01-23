package com.transcendence.chat;

import com.transcendence.chat.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO message, Principal principal) {
        // Principal is populated by your JwtChannelInterceptor
        message.setSender(principal.getName());
        message.setTimestamp(LocalDateTime.now());

        // Save to SQLite asynchronously through the service
        chatService.saveMessage(message);

        return message;
    }
}