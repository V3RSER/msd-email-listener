package com.example.demo.domain.repository;

import com.example.demo.domain.model.UserConnection;
import java.util.Optional;

public interface UserConnectionRepository {
    Optional<UserConnection> findByUserId(String userId);
    UserConnection save(UserConnection userConnection);
}
