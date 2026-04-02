package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.model.UserConnection;
import com.example.demo.infrastructure.persistence.entity.UserConnectionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConnectionMapper {
    UserConnection toDomain(UserConnectionEntity entity);
    UserConnectionEntity toEntity(UserConnection domain);
}
