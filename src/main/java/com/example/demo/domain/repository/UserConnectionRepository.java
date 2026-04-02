package com.example.demo.domain.repository;

import com.example.demo.domain.model.UserConnection;
import reactor.core.publisher.Mono;

public interface UserConnectionRepository {
    Mono<UserConnection> findByUserId(String userId);

    Mono<UserConnection> save(UserConnection userConnection);
}
