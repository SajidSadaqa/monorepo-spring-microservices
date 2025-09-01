package com.example.user.dto;

import com.example.user.domain.User;
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
  public static UserResponse fromEntity(User user) {
    return new UserResponse(
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
