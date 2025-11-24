package com.transcendence.game.arkanoid;

import com.transcendence.entity.ArkanoidScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArkanoidScoreRepository extends JpaRepository<ArkanoidScore, Long> {

    // Get score history for a user (most recent first)
    List<ArkanoidScore> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Count total games
    long countByUserId(Long userId);
}