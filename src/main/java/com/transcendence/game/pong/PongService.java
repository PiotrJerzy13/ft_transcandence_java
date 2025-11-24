package com.transcendence.game.pong;

import com.transcendence.entity.PongMatch;
import com.transcendence.entity.UserStats;
import com.transcendence.game.pong.dto.*;
import com.transcendence.stats.UserStatsService;
import com.transcendence.stats.dto.SaveScoreResponse;
import com.transcendence.stats.dto.UserStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PongService {

    @Autowired
    private PongMatchRepository pongMatchRepository;

    @Autowired
    private UserStatsService userStatsService;

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
                .mode(mode)
                .score(request.getScore())
                .opponentScore(request.getOpponentScore())
                .winner(request.getWinner())
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

        // 4. Return response (achievements always empty)
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
                .collect(Collectors.toList());

        return new PongHistoryResponse(history);
    }

}
