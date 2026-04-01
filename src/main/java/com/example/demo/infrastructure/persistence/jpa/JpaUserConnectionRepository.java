package com.example.demo.infrastructure.persistence.jpa;

import com.example.demo.infrastructure.persistence.entity.UserConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUserConnectionRepository extends JpaRepository<UserConnectionEntity, String> {
    Optional<UserConnectionEntity> findByUserId(String userId);
}
