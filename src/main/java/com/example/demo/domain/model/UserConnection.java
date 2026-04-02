package com.example.demo.domain.model;

import java.util.UUID;
import lombok.Data;

@Data
public class UserConnection {
    private UUID id;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private java.time.OffsetDateTime tokenExpiration;
}
