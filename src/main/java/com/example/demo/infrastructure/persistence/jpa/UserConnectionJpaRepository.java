package com.example.demo.infrastructure.persistence.jpa;

import com.example.demo.infrastructure.persistence.entity.UserConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserConnectionJpaRepository extends JpaRepository<UserConnectionEntity, Long> {
    Optional<UserConnectionEntity> findByUserId(String userId);
}
