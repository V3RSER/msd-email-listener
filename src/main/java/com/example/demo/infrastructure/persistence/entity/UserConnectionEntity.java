package com.example.demo.infrastructure.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("user_connections")
public class UserConnectionEntity {

    @Id
    private UUID id;

    private String provider;

    private String providerId;

    private String email;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime expiresIn;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
