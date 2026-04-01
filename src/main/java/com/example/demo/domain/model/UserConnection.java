package com.example.demo.domain.model;

import lombok.Data;

@Data
public class UserConnection {
    private String id;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private java.time.OffsetDateTime tokenExpiration;
}
