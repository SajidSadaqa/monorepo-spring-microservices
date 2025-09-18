package com.microservices.user.application.dto;

import com.microservices.user.domain.entities.UserEntity;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
  UUID id,
  String username,
  String email,
  boolean enabled,
  Instant createdAt,
  Instant updatedAt,
  Set<String> roles
) {
  public static UserResponse fromEntity(UserEntity userEntity) {
    return new UserResponse(
      userEntity.getId(),
      userEntity.getUsername(),
      userEntity.getEmail(),
      userEntity.isEnabled(),
      userEntity.getCreatedAt(),
      userEntity.getUpdatedAt(),
      null
    );
  }
}
