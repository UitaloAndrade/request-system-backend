package com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.mapper;

import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.entity.UserJpaEntity;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    public User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}