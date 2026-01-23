package com.transcendence.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data // This provides getters, setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {

    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private String type;
}