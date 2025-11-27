package com.transcendence.user;

import com.transcendence.entity.User;
import com.transcendence.entity.UserStats;
import com.transcendence.stats.UserStatsRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;

    public UserService(UserRepository userRepository,
                       UserStatsRepository userStatsRepository) {
        this.userRepository = userRepository;
        this.userStatsRepository = userStatsRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserStats getStatsByUserId(Long id) {
        return userStatsRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Stats not found"));
    }
}
