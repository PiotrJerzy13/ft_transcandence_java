package com.transcendence.chat;

import com.transcendence.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    // Fetch the most recent messages for the lobby history
    List<ChatMessage> findTop50ByOrderByTimestampDesc();
}
