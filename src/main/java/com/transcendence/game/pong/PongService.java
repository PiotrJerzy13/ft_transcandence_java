package com.transcendence.game.pong;

import com.transcendence.entity.PongMatch;
import com.transcendence.entity.UserStats;
import com.transcendence.game.pong.dto.*;
import com.transcendence.stats.UserStatsService;
import com.transcendence.stats.dto.SaveScoreResponse;
import com.transcendence.stats.dto.UserStatsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class PongService {

    private final PongMatchRepository pongMatchRepository;
    private final UserStatsService userStatsService;

    public PongService(PongMatchRepository pongMatchRepository,
                       UserStatsService userStatsService) {
        this.pongMatchRepository = pongMatchRepository;
        this.userStatsService = userStatsService;
    }

    /**
     * Save a Pong score
     */
    public SaveScoreResponse saveScore(Long userId, String mode, PongScoreRequest request)
    {
        // 1. Calculate XP (simple formula)
        boolean won = "player".equals(request.getWinner());
        int xpEarned = request.getXpEarned();

        // 2. Save the match
        PongMatch match = PongMatch.builder()
                .userId(userId)
                .mode(PongMatch.Mode.fromValue(mode)) // "one-player" → Mode.ONE_PLAYER
                .score(request.getScore())
                .opponentScore(request.getOpponentScore())
                .winner(PongMatch.Winner.valueOf(request.getWinner()))
                .duration(request.getDuration() != null ? request.getDuration() : 0)
                .xpEarned(xpEarned)
                .build();

        pongMatchRepository.save(match);

        // 3. Update user stats
        UserStats updatedStats = userStatsService.updateAfterGame(
                userId,
                won,
                match.getDuration(),
                xpEarned
        );

        return new SaveScoreResponse(
                true,
                "Score saved successfully",
                List.of(),  // Empty achievements
                UserStatsDto.fromEntity(updatedStats)
        );
    }

    /**
     * Get Pong match history
     */
    public PongHistoryResponse getHistory(Long userId) {
        List<PongMatch> matches = pongMatchRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<PongHistoryItemDto> history = matches.stream()
                .map(PongHistoryItemDto::fromEntity)
                .toList();

        return new PongHistoryResponse(history);
    }
}