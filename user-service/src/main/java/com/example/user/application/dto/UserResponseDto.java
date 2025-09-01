package com.example.user.application.dto;

import com.example.user.domain.model.User;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponseDto(
  UUID id,
  String username,
  String email,
  boolean enabled,
  Instant createdAt,
  Instant updatedAt,
  Set<String> roles
) {
  public static UserResponseDto fromEntity(User user) {
    return new UserResponseDto(
      user.getId(),
      user.getUsername(),
      user.getEmail(),
      user.isEnabled(),
      user.getCreatedAt(),
      user.getUpdatedAt(),
      null
    );
  }
}
