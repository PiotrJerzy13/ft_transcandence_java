package com.transcendence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //uses user_id from user table
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_games")
    private Integer totalGames = 0;

    private Integer wins = 0;
    private Integer losses = 0;

    @Column(name = "win_streak")
    private Integer winStreak = 0;

    @Column(name = "best_streak")
    private Integer bestStreak = 0;

    @Column(name = "total_play_time")
    private Integer totalPlayTime = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rank rank = Rank.Novice;

    private Integer level = 1;
    private Integer xp = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Helper - real logic uses actual stats
    public double getWinRate() {
        if (totalGames == 0) return 0.0;
        return (double) wins / totalGames;
    }
//lowercase because db expects it in that way
    public enum Rank {
        Novice,
        Amateur,
        Pro,
        Expert,
        Master
    }
}
