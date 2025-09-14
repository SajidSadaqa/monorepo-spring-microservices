package com.microservices.user.interfaces.rest;

import com.microservices.user.application.dto.PageResponse;
import com.microservices.user.application.dto.UserResponse;
import com.microservices.user.application.service.UserApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
@Slf4j

public class UserRestController {
  private final UserApplicationService svc;

  public UserRestController(UserApplicationService svc) {
    this.svc = svc;
  }

  @GetMapping
  public PageResponse<UserResponse> list(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(defaultValue = "createdAt,desc") String sort) {
    log.info("User list request - Page: {}, Size: {}, Sort: {}", page, size, sort);

    try {
      Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sort.split(",")[0]).descending());
      PageResponse<UserResponse> response = svc.list(pageable);
      log.info("User list retrieved - {} users returned", response.content().size());
      return response;
    } catch (Exception e) {
      log.error("Failed to retrieve user list: {}", e.getMessage(), e);
      throw e;
    }
  }

  @GetMapping("/{id}")
  public UserResponse get(@PathVariable UUID id) {
    log.info("Get user request for ID: {}", id);

    try {
      UserResponse response = svc.getById(id);
      log.info("User retrieved successfully: {}", id);
      return response;
    } catch (Exception e) {
      log.error("Failed to retrieve user {}: {}", id, e.getMessage());
      throw e;
    }
  }
}
