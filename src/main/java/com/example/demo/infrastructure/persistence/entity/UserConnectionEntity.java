package com.example.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "user_connections")
public class UserConnectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, length = 2048)
    private String accessToken;

    @Column(nullable = false, length = 2048)
    private String refreshToken;

    @Column(nullable = false)
    private OffsetDateTime tokenExpiration;
}
