package com.transcendence.stats;

import com.transcendence.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {

    Optional<UserStats> findByUserId(Long userId);

    // For leaderboard - order by XP
    List<UserStats> findAllByOrderByXpDescLevelDesc();
}