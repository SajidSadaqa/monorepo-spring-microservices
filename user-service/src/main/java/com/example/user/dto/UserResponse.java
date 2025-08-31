package com.example.user.dto;

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
  Set<String> roles) {}
