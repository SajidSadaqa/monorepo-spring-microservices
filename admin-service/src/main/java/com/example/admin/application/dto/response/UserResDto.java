package com.example.admin.application.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResDto(
  UUID id,
  String username,
  String email,
  boolean enabled,
  Instant createdAt,
  Instant updatedAt,
  Set<String> roles) {}
