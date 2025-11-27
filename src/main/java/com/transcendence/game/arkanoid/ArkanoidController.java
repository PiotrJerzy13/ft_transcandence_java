package com.transcendence.game.arkanoid;

import com.transcendence.game.arkanoid.dto.ArkanoidHistoryResponse;
import com.transcendence.game.arkanoid.dto.ArkanoidScoreRequest;
import com.transcendence.stats.dto.SaveScoreResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/arkanoid")
@CrossOrigin(origins = "http://localhost:5173")  // Allow React dev server
public class ArkanoidController {

    private final ArkanoidService arkanoidService;

    /**
     * Save Arkanoid score
     * POST /api/arkanoid/score
     */
    public ArkanoidController (ArkanoidService arkanoidService){
        this.arkanoidService = arkanoidService;
    }
    @PostMapping("/score")
    public ResponseEntity<SaveScoreResponse> saveScore(
            @Valid @RequestBody ArkanoidScoreRequest request
    ) {
        // TODO: Replace with JWT userId after security is implemented
        Long userId = 1L;  // Hardcoded for now

        log.info("Saving Arkanoid score for user {}: score={}, level={}",
                userId, request.getScore(), request.getLevelReached());

        SaveScoreResponse response = arkanoidService.saveScore(userId, request);

        log.info("Arkanoid score saved successfully. New XP: {}",
                response.getUserStats().getXp());

        return ResponseEntity.ok(response);
    }

    /**
     * Get Arkanoid score history
     * GET /api/arkanoid/history
     */
    @GetMapping("/history")
    public ResponseEntity<ArkanoidHistoryResponse> getHistory() {
        // TODO: Replace with JWT userId after security is implemented
        Long userId = 1L;  // Hardcoded for now

        log.info("Fetching Arkanoid history for user {}", userId);

        ArkanoidHistoryResponse response = arkanoidService.getHistory(userId);

        log.info("Retrieved {} Arkanoid scores", response.getHistory().size());

        return ResponseEntity.ok(response);
    }
}