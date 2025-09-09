package com.example.user.interfaces.rest;

import com.example.user.application.dto.PageResponse;
import com.example.user.application.dto.UserResponse;
import com.example.user.application.service.UserApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserRestController {
  private final UserApplicationService svc;

  public UserRestController(UserApplicationService svc) { this.svc = svc; }

  @GetMapping
  public PageResponse<UserResponse> list(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(defaultValue = "createdAt,desc") String sort) {
    Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sort.split(",")[0]).descending());
    return svc.list(pageable);
  }

  @GetMapping("/{id}")
  public UserResponse get(@PathVariable UUID id) {
    return svc.getById(id);
  }
}
