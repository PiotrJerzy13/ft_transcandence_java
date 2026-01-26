package com.transcendence.chat;

import com.transcendence.chat.dto.ChatMessageDTO;
import com.transcendence.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    @Async
    @Transactional
    public void saveMessage(ChatMessageDTO messageDTO) {
        ChatMessage entity = new ChatMessage();
        entity.setSender(messageDTO.getSender());
        entity.setContent(messageDTO.getContent());
        entity.setTimestamp(messageDTO.getTimestamp());
        entity.setType(messageDTO.getType());

        chatRepository.save(entity);
        log.info("Message saved to database from: {}", messageDTO.getSender());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getRecentMessages(int limit) {
        List<ChatMessage> messages = chatRepository.findRecentMessages(limit);

        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ChatMessageDTO convertToDTO(ChatMessage entity) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSender(entity.getSender());
        dto.setContent(entity.getContent());
        dto.setTimestamp(entity.getTimestamp());
        dto.setType(entity.getType());
        return dto;
    }
}