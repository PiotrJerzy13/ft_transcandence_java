package com.transcendence.entity;

import jakarta.persistence.*;
// REMOVED lombok.AllArgsConstructor
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data // Use @Data for getters/setters
// NOTE: @Builder at class level is intentionally omitted as it's on the constructor
@NoArgsConstructor // Required by JPA
public class User {

    // ... fields remain unchanged ...

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


//    @UpdateTimestamp
//    @Column(name = "updated_at", nullable = false)
//    private LocalDateTime updatedAt;
//
//    @CreationTimestamp
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;

    @Column(name = "created_at",
            nullable = false,
            updatable = false,
            insertable = false)
    private String createdAt;

    @Column(name = "updated_at", nullable = false,insertable = false)
    private String updatedAt;
    // This is the correct builder pattern implementation:
    @Builder
    public User(String username, String email, String passwordHash, String avatarUrl, String status) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.avatarUrl = avatarUrl;
        this.status = status;
    }

    // The explicit getters you added are now redundant because of @Data.
    // You can remove them to keep the code clean, but they won't cause the error.
}