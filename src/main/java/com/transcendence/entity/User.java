package com.transcendence.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor // Required by JPA
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, length = 255)
    private String username;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at",
            nullable = false,
            updatable = false,
            insertable = false)
    private String createdAt;

    @Column(name = "updated_at", nullable = false,insertable = false)
    private String updatedAt;

    @Builder
    public User(String username, String email, String passwordHash, String avatarUrl, String status) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.avatarUrl = avatarUrl;
        this.status = status;
    }

}