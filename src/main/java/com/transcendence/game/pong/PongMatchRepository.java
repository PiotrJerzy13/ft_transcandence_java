package com.transcendence.game.pong;

import com.transcendence.entity.PongMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PongMatchRepository extends JpaRepository<PongMatch, Long> {

    // Get match history for a user (most recent first)
    List<PongMatch> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Count total matches
    long countByUserId(Long userId);
}