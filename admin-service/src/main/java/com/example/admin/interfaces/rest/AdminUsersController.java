package com.example.admin.interfaces.rest;

import com.example.admin.application.dto.response.PageResDto;
import com.example.admin.application.dto.response.UserResDto;
import com.example.admin.infrastructure.external.UserDirectoryClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin Users Proxy")
public class AdminUsersController {

  private final UserDirectoryClient client;

  public AdminUsersController(UserDirectoryClient client) {
    this.client = client;
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public UserResDto getOne(@PathVariable UUID id) {
    return client.getUserById(id);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public PageResDto<UserResDto> list(@RequestParam int page, @RequestParam int size, @RequestParam(defaultValue = "createdAt,desc") String sort) {
    return client.listUsers(page, size, sort);
  }
}
