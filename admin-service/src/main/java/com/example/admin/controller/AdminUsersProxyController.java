package com.example.admin.controller;

import com.example.admin.dto.PageResponse;
import com.example.admin.dto.UserResponse;
import com.example.admin.feign.UserDirectoryClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin Users Proxy")
public class AdminUsersProxyController {

  private final UserDirectoryClient client;

  public AdminUsersProxyController(UserDirectoryClient client) {
    this.client = client;
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public UserResponse getOne(@PathVariable UUID id) {
    return client.getUserById(id);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public PageResponse<UserResponse> list(@RequestParam int page, @RequestParam int size, @RequestParam(defaultValue = "createdAt,desc") String sort) {
    return client.listUsers(page, size, sort);
  }
}
