package com.microservices.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.microservices.user.application.validation.ValidPassword;

public record AuthReq(
  @NotBlank(message = "{auth.username.required}")
  @Pattern(regexp = "^[A-Za-z0-9._-]{3,30}$", message = "{auth.username.invalid}")
  String username,

  @NotBlank(message = "{auth.password.required}")
  @ValidPassword(message = "{auth.password.invalid}")
  String password
) {}
