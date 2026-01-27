package com.transcendence.chat;

import com.transcendence.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop50ByOrderByTimestampDesc();

    @Query("SELECT c FROM ChatMessage c ORDER BY c.timestamp DESC")
    List<ChatMessage> findRecentMessages(@Param("limit") int limit);
}
