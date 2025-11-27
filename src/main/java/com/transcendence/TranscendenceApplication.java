package com.transcendence;

import com.transcendence.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TranscendenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TranscendenceApplication.class, args);
    }

    private static final Logger log = LoggerFactory.getLogger(TranscendenceApplication.class);
    @Bean
    CommandLineRunner testUsers(UserRepository userRepository) {
        return args -> {
            log.info("=== Testing SQLite users table ===");
            var users = userRepository.findAll();
            log.info("Total users: {}", users.size());
            users.stream()
                    .limit(5)
                    .forEach(u -> log.debug("{} | {} | {} | {}",
                            u.getId(), u.getUsername(), u.getEmail(), u.getStatus()));
            log.info("=== Done ===");
        };
    }
}

