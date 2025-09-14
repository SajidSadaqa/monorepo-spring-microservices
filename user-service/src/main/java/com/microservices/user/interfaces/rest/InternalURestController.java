package com.microservices.user.interfaces.rest;

import com.microservices.user.application.dto.UserResponse;
import com.microservices.user.application.service.UserApplicationService;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
public class InternalURestController {
  private final UserApplicationService svc;
  public InternalURestController(UserApplicationService svc) { this.svc = svc; }

  @GetMapping("/{id}")
  public UserResponse getInternal(@PathVariable UUID id) {
    return svc.getInternalById(id);
  }
}
