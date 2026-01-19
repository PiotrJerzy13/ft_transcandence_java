package com.transcendence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlacklistedToken {

    @Id
    @Column(length = 512)
    private String token;

    private LocalDateTime expiryDate;
}
