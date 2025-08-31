package com.example.user.controller;

import com.example.user.dto.UserResponse;
import com.example.user.service.UserService;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {
  private final UserService svc;
  public InternalUserController(UserService svc) { this.svc = svc; }

  @GetMapping("/{id}")
  public UserResponse getInternal(@PathVariable UUID id) {
    return svc.getInternalById(id);
  }
}
