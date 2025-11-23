package com.transcendence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "arkanoid_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArkanoidScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "xp_earned")
    private Integer xpEarned = 0;

    @Column(name = "level_reached", nullable = false)
    private Integer levelReached;

    @Column(name = "blocks_destroyed")
    private Integer blocksDestroyed = 0;

    @Column(name = "power_ups_collected")
    private Integer powerUpsCollected = 0;

    @Column(name = "duration")
    private Integer duration = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---------- Helper Methods ----------

    /**
     * Calculate average score per level reached
     * @return score divided by levels reached, or 0 if no levels
     */
    public double getAverageScorePerLevel() {
        if (levelReached == null || levelReached == 0) {
            return 0.0;
        }
        return (double) score / levelReached;
    }

    /**
     * Calculate blocks destroyed per minute
     * @return blocks per minute, or 0 if no duration
     */
    public double getBlocksPerMinute() {
        if (duration == null || duration == 0) {
            return 0.0;
        }
        return (blocksDestroyed * 60.0) / duration;
    }

    /**
     * Check if score meets or exceeds threshold
     * @param threshold minimum score
     * @return true if score >= threshold
     */
    public boolean isHighScore(int threshold) {
        return score != null && score >= threshold;
    }

    /**
     * Check if player reached a specific level
     * @param level target level
     * @return true if levelReached >= level
     */
    public boolean reachedLevel(int level) {
        return levelReached != null && levelReached >= level;
    }

    /**
     * Check if this was a perfect game (all blocks destroyed)
     * @param totalBlocks total blocks in the game
     * @return true if all blocks were destroyed
     */
    public boolean isPerfectGame(int totalBlocks) {
        return blocksDestroyed != null && blocksDestroyed >= totalBlocks;
    }

    /**
     * Calculate power-up collection rate
     * @return power-ups per minute, or 0 if no duration
     */
    public double getPowerUpsPerMinute() {
        if (duration == null || duration == 0) {
            return 0.0;
        }
        return (powerUpsCollected * 60.0) / duration;
    }
}