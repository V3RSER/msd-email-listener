package com.example.demo.infrastructure.persistence.r2dbc;

import com.example.demo.infrastructure.persistence.entity.UserConnectionEntity;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserConnectionR2dbcRepository extends R2dbcRepository<UserConnectionEntity, UUID> {
    Mono<UserConnectionEntity> findByUserId(String userId);
}
