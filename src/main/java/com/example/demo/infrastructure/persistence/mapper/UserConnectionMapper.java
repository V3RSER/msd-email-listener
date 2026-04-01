package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.infrastructure.persistence.entity.UserConnectionEntity;
import org.springframework.stereotype.Component;

@Component
public class UserConnectionMapper {

    public UserConnectionEntity toEntity(UserConnection model) {
        if (model == null) {
            return null;
        }
        UserConnectionEntity entity = new UserConnectionEntity();
        entity.setId(model.getId());
        entity.setUserId(model.getUserId());
        entity.setAccessToken(model.getAccessToken());
        entity.setRefreshToken(model.getRefreshToken());
        entity.setTokenExpiration(model.getTokenExpiration());
        return entity;
    }

    public UserConnection toModel(UserConnectionEntity entity) {
        if (entity == null) {
            return null;
        }
        UserConnection model = new UserConnection();
        model.setId(entity.getId());
        model.setUserId(entity.getUserId());
        model.setAccessToken(entity.getAccessToken());
        model.setRefreshToken(entity.getRefreshToken());
        model.setTokenExpiration(entity.getTokenExpiration());
        return model;
    }
}
