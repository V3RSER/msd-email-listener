package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.domain.repository.UserConnectionRepository;
import com.example.demo.infrastructure.persistence.jpa.JpaUserConnectionRepository;
import com.example.demo.infrastructure.persistence.mapper.UserConnectionMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserConnectionRepositoryAdapter implements UserConnectionRepository {

    private final JpaUserConnectionRepository jpaRepository;
    private final UserConnectionMapper mapper;

    public UserConnectionRepositoryAdapter(JpaUserConnectionRepository jpaRepository, UserConnectionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<UserConnection> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toModel);
    }

    @Override
    public UserConnection save(UserConnection userConnection) {
        var entity = mapper.toEntity(userConnection);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toModel(savedEntity);
    }
}
