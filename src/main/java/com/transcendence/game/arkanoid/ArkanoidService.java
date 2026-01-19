package com.transcendence.game.arkanoid;

import com.transcendence.entity.ArkanoidScore;
import com.transcendence.entity.UserStats;
import com.transcendence.game.arkanoid.dto.*;
import com.transcendence.stats.UserStatsService;
import com.transcendence.stats.dto.SaveScoreResponse;
import com.transcendence.stats.dto.UserStatsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ArkanoidService {

    private final ArkanoidScoreRepository arkanoidScoreRepository;
    private final UserStatsService userStatsService;

    public ArkanoidService(ArkanoidScoreRepository arkanoidScoreRepository,
                           UserStatsService userStatsService) {
        this.arkanoidScoreRepository = arkanoidScoreRepository;
        this.userStatsService = userStatsService;
    }

    public SaveScoreResponse saveScore(Long userId, ArkanoidScoreRequest request) {

        int xpEarned = request.getXpEarned();

        // 2. Save score row
        ArkanoidScore score = ArkanoidScore.builder()
                .userId(userId)
                .score(request.getScore())
                .levelReached(request.getLevelReached())
                .xpEarned(xpEarned)
                .duration(request.getDuration() != null ? request.getDuration() : 0)
                .blocksDestroyed(request.getBlocksDestroyed() != null ? request.getBlocksDestroyed() : 0)
                .powerUpsCollected(request.getPowerUpsCollected() != null ? request.getPowerUpsCollected() : 0)
                .build();

        arkanoidScoreRepository.save(score);

        // 3. Update stats
        UserStats updatedStats = userStatsService.updateAfterGame(
                userId,
                true,
                score.getDuration(),
                xpEarned
        );

        // 4. Build response
        return new SaveScoreResponse(
                true,
                "Score saved successfully",
                List.of(),
                UserStatsDto.fromEntity(updatedStats)
        );
    }

    public ArkanoidHistoryResponse getHistory(Long userId) {
        List<ArkanoidScore> scores = arkanoidScoreRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        List<ArkanoidHistoryItemDto> history = scores.stream()
                .map(ArkanoidHistoryItemDto::fromEntity)
                .toList();

        return new ArkanoidHistoryResponse(history);
    }
}
