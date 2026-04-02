package com.example.demo.domain.model;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserConnection {
    private UUID id;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private Instant accessTokenIssuedAt;
    private Instant accessTokenExpiresAt;
}
