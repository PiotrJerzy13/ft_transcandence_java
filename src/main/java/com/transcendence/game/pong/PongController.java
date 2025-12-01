package com.transcendence.game.pong;

import com.transcendence.game.pong.dto.PongHistoryResponse;
import com.transcendence.game.pong.dto.PongScoreRequest;
import com.transcendence.stats.dto.SaveScoreResponse;
import com.transcendence.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/pong")
@CrossOrigin(origins = "http://localhost:5173")
public class PongController {

    private final PongService pongService;
    private final UserRepository userRepository;

    public PongController(PongService pongService, UserRepository userRepository) {
        this.pongService = pongService;
        this.userRepository = userRepository;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // The principal is the UserDetails object loaded during JWT validation
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."))
                .getId();
    }

    /**
     * Save Pong score
     * POST /api/pong/score?mode=one-player
     */
    @PostMapping("/score")
    public ResponseEntity<SaveScoreResponse> saveScore(
            @RequestParam(defaultValue = "one-player") String mode,
            @Valid @RequestBody PongScoreRequest request
    ) {
        Long userId = getAuthenticatedUserId();

        log.info("Saving Pong score for user {}: mode={}, score={}-{}, winner={}",
                userId, mode, request.getScore(),
                request.getOpponentScore(), request.getWinner());

        SaveScoreResponse response = pongService.saveScore(userId, mode, request);

        log.info("Pong score saved successfully. New XP: {}",
                response.getUserStats().getXp());

        return ResponseEntity.ok(response);
    }

    /**
     * Get Pong match history
     * GET /api/pong/history
     */
    @GetMapping("/history")
    public ResponseEntity<PongHistoryResponse> getHistory() {
        Long userId = getAuthenticatedUserId();

        log.info("Fetching Pong history for user {}", userId);

        PongHistoryResponse response = pongService.getHistory(userId);

        log.info("Retrieved {} Pong matches", response.getHistory().size());

        return ResponseEntity.ok(response);
    }
}
