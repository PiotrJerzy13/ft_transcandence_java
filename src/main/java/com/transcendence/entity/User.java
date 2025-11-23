package com.transcendence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false,unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,unique = true)
    private Status status =Status.offline;

    @Column(name = "created_at",unique = true)
    private LocalDateTime created_at=  LocalDateTime.now();

    @Column(name = "updated_at",unique = true)
    private LocalDateTime updated_at=  LocalDateTime.now();

    public enum Status{
        offline,
        online,in_game
    }

}
