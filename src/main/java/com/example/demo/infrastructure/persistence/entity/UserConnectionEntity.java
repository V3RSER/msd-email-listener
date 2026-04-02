package com.example.demo.infrastructure.persistence.entity;

import java.time.OffsetDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("user_connections")
public class UserConnectionEntity {

    @Id
    private String id;

    private String userId;

    private String accessToken;

    private String refreshToken;

    private OffsetDateTime tokenExpiration;
}
