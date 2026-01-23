package com.transcendence.chat;

import com.transcendence.chat.dto.ChatMessageDTO;
import com.transcendence.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    @Async
    public void saveMessage(ChatMessageDTO dto) {
        // Map DTO to Entity
        ChatMessage entity = ChatMessage.builder()
                .sender(dto.getSender())
                .content(dto.getContent())
                .timestamp(dto.getTimestamp())
                .type(dto.getType())
                .build();

        chatRepository.save(entity);
    }
}
