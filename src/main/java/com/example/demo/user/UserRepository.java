package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<com.example.demo.user.User, Long> {
    // for now we just need findAll()
}

