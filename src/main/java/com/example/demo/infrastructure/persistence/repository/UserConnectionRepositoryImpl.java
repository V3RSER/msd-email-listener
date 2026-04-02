package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.persistence.mapper.UserConnectionMapper;
import com.example.demo.infrastructure.persistence.r2dbc.UserConnectionR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserConnectionRepositoryImpl implements UserConnectionRepository {

    private final UserConnectionR2dbcRepository r2dbcRepository;
    private final UserConnectionMapper mapper;

    @Override
    public Mono<UserConnection> findByUserId(String userId) {
        return r2dbcRepository.findByUserId(userId).map(mapper::toModel);
    }

    @Override
    public Mono<UserConnection> save(UserConnection userConnection) {
        return Mono.just(userConnection)
                .map(mapper::toEntity)
                .flatMap(r2dbcRepository::save)
                .map(mapper::toModel);
    }
}
