package com.example.admin.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminLoginReqDto(
  @NotBlank(message = "{auth.username.required}")
  @Pattern(regexp = "^[A-Za-z0-9._-]{3,30}$", message = "{auth.username.invalid}")
  String username,
  @NotBlank(message = "{auth.password.required}")
  @Pattern(
    regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,72}$",
    message = "{auth.password.weak}")
  String password) {}
