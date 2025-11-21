package com.example.demo;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner testUsers(UserRepository userRepository) {
        return args -> {
            System.out.println("=== Testing SQLite users table ===");
            var users = userRepository.findAll();
            System.out.println("Total users: " + users.size());
            users.stream()
                    .limit(5)
                    .forEach(u -> System.out.println(
                            u.getId() + " | " +
                                    u.getUsername() + " | " +
                                    u.getEmail() + " | " +
                                    u.getStatus()
                    ));
            System.out.println("=== Done ===");
        };
    }
}

