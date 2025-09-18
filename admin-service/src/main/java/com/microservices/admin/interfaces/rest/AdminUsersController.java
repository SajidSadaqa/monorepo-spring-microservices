package com.microservices.admin.interfaces.rest;

import com.microservices.admin.application.dto.response.PageResDto;
import com.microservices.admin.application.dto.response.UserResDto;
import com.microservices.admin.infrastructure.external.UserDirectoryClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Admin Users Proxy")
@Validated
public class AdminUsersController {

  private static final Logger logger = LoggerFactory.getLogger(AdminUsersController.class);
  private final UserDirectoryClient client;

  public AdminUsersController(UserDirectoryClient client) {
    this.client = client;
    logger.info("AdminUsersController initialized with UserDirectoryClient");
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public UserResDto getOne(@PathVariable("id") UUID uuid) {
    logger.info("🔍 GET USER BY ID - UUID: {}", uuid);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    logger.debug("👤 Current user: {}, authorities: {}", auth.getName(), auth.getAuthorities());

    try {
      UserResDto result = client.getUserById(uuid);
      logger.info("✅ Successfully retrieved user: {}", uuid);
      return result;
    } catch (Exception e) {
      logger.error("❌ Failed to retrieve user {}: {}", uuid, e.getMessage(), e);
      throw e;
    }
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

    logger.info("📋 LIST USERS REQUEST - Page: {}, Size: {}, Sort: {}", page, size, sort);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    logger.info("👤 Admin user: {}, authorities: {}", auth.getName(), auth.getAuthorities());

    try {
      logger.info("🔄 Making S2S call to user-service...");
      PageResDto<UserResDto> result = client.listUsers(page, size, sort);

      // Fix: Access record fields directly, not with getter methods
      logger.info("✅ Successfully retrieved {} users from user-service (page {}/{}, total: {})",
        result.content() != null ? result.content().size() : 0,  // Use content() not getContent()
        result.page() + 1,
        result.totalPages(),
        result.totalElements());

      return result;
    } catch (Exception e) {
      logger.error("❌ S2S call to user-service failed: {}", e.getMessage(), e);
      throw e;
    }
  }
}
