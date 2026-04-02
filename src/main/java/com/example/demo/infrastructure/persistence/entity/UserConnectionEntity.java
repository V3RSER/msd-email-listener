package com.example.demo.infrastructure.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("user_connections")
public class UserConnectionEntity {

    private UUID id;

    @Column("user_id")
    private String userId;

    @Column("access_token")
    private String accessToken;

    @Column("refresh_token")
    private String refreshToken;

    @Column("token_expiration")
    private OffsetDateTime tokenExpiration;
}
