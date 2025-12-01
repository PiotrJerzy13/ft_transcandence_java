package com.transcendence.game.arkanoid;

import com.transcendence.game.arkanoid.dto.ArkanoidHistoryResponse;
import com.transcendence.game.arkanoid.dto.ArkanoidScoreRequest;
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
@RequestMapping("/api/arkanoid")
@CrossOrigin(origins = "http://localhost:5173")
public class ArkanoidController {

    private final ArkanoidService arkanoidService;
    private final UserRepository userRepository;

    /**
     * Save Arkanoid score
     * POST /api/arkanoid/score
     */
    public ArkanoidController (ArkanoidService arkanoidService, UserRepository userRepository){
        this.arkanoidService = arkanoidService;
        this.userRepository = userRepository;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // The principal is the UserDetails object loaded during JWT validation
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Look up the actual User ID (Long) from the repository
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."))
                .getId();
    }

    @PostMapping("/score")
    public ResponseEntity<SaveScoreResponse> saveScore(
            @Valid @RequestBody ArkanoidScoreRequest request
    ) {
//        Long userId = 1L;
        Long userId = getAuthenticatedUserId();

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

//        Long userId = 1L;
        Long userId = getAuthenticatedUserId();

        log.info("Fetching Arkanoid history for user {}", userId);

        ArkanoidHistoryResponse response = arkanoidService.getHistory(userId);

        log.info("Retrieved {} Arkanoid scores", response.getHistory().size());

        return ResponseEntity.ok(response);
    }
}