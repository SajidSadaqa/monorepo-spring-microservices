package com.microservices.admin.interfaces.rest;

import com.microservices.admin.application.dto.response.PageResDto;
import com.microservices.admin.application.dto.response.UserResDto;
import com.microservices.admin.infrastructure.external.UserDirectoryClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Admin Users Proxy")
@Validated
public class AdminUsersController {

  private final UserDirectoryClient client;

  public AdminUsersController(UserDirectoryClient client) {
    this.client = client;
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public UserResDto getOne(@PathVariable("id") UUID uuid) {
    return client.getUserById(uuid);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public PageResDto<UserResDto> list(
    @RequestParam(name = "page")
    @Min(value = 0, message = "Page number must be greater than or equal to 0")
    int page,

    @RequestParam(name = "size")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    int size,

    @RequestParam(defaultValue = "createdAt,desc", name = "sort")
    @Pattern(regexp = "^[a-zA-Z]+,(asc|desc)$",
      message = "Sort format must be 'fieldName,direction' where direction is 'asc' or 'desc'")
    String sort) {
    return client.listUsers(page, size, sort);
  }
}
