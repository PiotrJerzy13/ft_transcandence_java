// LeaderboardController.java
package com.transcendence.leaderboard;

import com.transcendence.leaderboard.dto.LeaderboardResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<LeaderboardResponseDTO> getLeaderboard() {
        try {
            LeaderboardResponseDTO response = leaderboardService.getLeaderboard();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LeaderboardResponseDTO errorResponse = LeaderboardResponseDTO.builder()
                    .error("Failed to load leaderboard")
                    .build();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}