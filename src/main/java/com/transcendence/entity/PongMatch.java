package com.transcendence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "pong_matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PongMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Convert(converter = ModeConverter.class)
    @Column(name = "mode", nullable = false)
    private Mode mode;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "opponent_score", nullable = false)
    private Integer opponentScore;

//    @Column(name = "winner", nullable = false)
//    private Winner winner;

    @Column(name = "xp_earned")
    private Integer xpEarned = 0;

    @Column(name = "total_xp")
    private Integer totalXp = 0;

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

    // ---------- Helper methods ----------

    public boolean isPlayerWin() {
        return Winner.player.equals(this.winner);
    }

    public boolean isOpponentWin() {
        return Winner.opponent.equals(this.winner);
    }

    public boolean isOnePlayerMode() {
        return Mode.ONE_PLAYER.equals(this.mode);
    }

    public boolean isTwoPlayerMode() {
        return Mode.TWO_PLAYER.equals(this.mode);
    }

    public Integer getScoreDifference() {
        return Math.abs(score - opponentScore);
    }

    // "Perfect game" = player wins and opponent has 0 points
    public boolean isPerfectGame() {
        return isPlayerWin() && opponentScore == 0;
    }

    // ---------- Enums + Converters ----------
    @RequiredArgsConstructor
    @Getter
    public enum Mode {
        ONE_PLAYER("one-player"),
        TWO_PLAYER("two-player");

        @JsonValue
        private final String value;

        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static Mode fromValue(String value) {
            for (Mode m : Mode.values()) {
                if (m.value.equals(value)) {
                    return m;
                }
            }
            throw new IllegalArgumentException("Invalid mode: " + value);
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "winner", nullable = false)
    private Winner winner;

    public enum Winner {
        player,    // lowercase to match database constraint
        opponent;  // lowercase to match database constraint
    }

    // JPA converters to map enums <-> DB TEXT (‘one-player’, ‘two-player’)
    @Converter
    public static class ModeConverter implements AttributeConverter<Mode, String> {
        @Override
        public String convertToDatabaseColumn(Mode attribute) {
            return attribute.getValue();
        }

        @Override
        public Mode convertToEntityAttribute(String dbData) {
            return dbData != null ? Mode.fromValue(dbData) : null;
        }
    }


}
