package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConnection {
    private UUID id;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private OffsetDateTime accessTokenIssuedAt;
    private OffsetDateTime accessTokenExpiresAt;

    public UserConnection(String userId) {
        this.userId = userId;
    }
}
