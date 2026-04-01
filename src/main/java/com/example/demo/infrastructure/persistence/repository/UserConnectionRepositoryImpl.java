package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.persistence.entity.UserConnectionEntity;
import com.example.demo.infrastructure.persistence.jpa.UserConnectionJpaRepository;
import com.example.demo.infrastructure.persistence.mapper.UserConnectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserConnectionRepositoryImpl implements UserConnectionRepository {

    private final UserConnectionJpaRepository jpaRepository;
    private final UserConnectionMapper mapper;

    @Override
    public Optional<UserConnection> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toModel);
    }

    @Override
    public UserConnection save(UserConnection userConnection) {
        UserConnectionEntity entity = mapper.toEntity(userConnection);
        UserConnectionEntity savedEntity = jpaRepository.save(entity);
        return mapper.toModel(savedEntity);
    }
}
