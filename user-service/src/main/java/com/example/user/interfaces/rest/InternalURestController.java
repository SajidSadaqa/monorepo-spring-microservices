package com.example.user.interfaces.rest;

import com.example.user.application.dto.UserResponseDto;
import com.example.user.application.service.UserApplicationService;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
public class InternalURestController {
  private final UserApplicationService svc;
  public InternalURestController(UserApplicationService svc) { this.svc = svc; }

  @GetMapping("/{id}")
  public UserResponseDto getInternal(@PathVariable UUID id) {
    return svc.getInternalById(id);
  }
}
