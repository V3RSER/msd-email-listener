package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.infrastructure.persistence.entity.UserConnectionEntity;
import org.mapstruct.Mapper;

@Mapper
public interface UserConnectionMapper {

    UserConnectionEntity toEntity(UserConnection model);

    UserConnection toModel(UserConnectionEntity entity);
}
