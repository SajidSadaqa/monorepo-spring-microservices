package com.microservices.user.application.service.impl;

import com.microservices.user.application.dto.UserResponse;
import com.microservices.user.application.mapper.UserMapper;
import com.microservices.user.application.service.UserApplicationService;
import com.microservices.user.application.dto.PageResponse;
import com.microservices.user.infrastructure.persistence.UserJpaRepository;
import com.microservices.user.application.util.RedisCacheService;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.microservices.user.interfaces.exception.ResourceNotFoundException;

@Service
@Slf4j
public class UserApplicationServiceImpl implements UserApplicationService {

  private final UserJpaRepository users;
  private final UserMapper mapper;
  private final RedisCacheService redisCacheService;

  public UserApplicationServiceImpl(UserJpaRepository users, UserMapper mapper, RedisCacheService redisCacheService) {
    this.users = users;
    this.mapper = mapper;
    this.redisCacheService = redisCacheService;
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public PageResponse<UserResponse> list(Pageable pageable) {
    log.info("Admin requesting user list - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

    String cacheKey = "users:list:" + pageable.getPageNumber() + ":" + pageable.getPageSize();
    PageResponse<UserResponse> cached = redisCacheService.getObject(cacheKey, PageResponse.class);
    if (cached != null) {
      return cached;
    }

    try {
      var page = users.findAll(pageable).map(mapper::toDto);
      PageResponse<UserResponse> response =
        new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());

      redisCacheService.putObject(cacheKey, response, 60); // cache for 1 minute
      return response;
    } catch (Exception e) {
      log.error("Error retrieving user list: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public UserResponse getById(UUID id) {
    log.info("Retrieving user by ID: {}", id);

    String cacheKey = "user:" + id;
    UserResponse cached = redisCacheService.getObject(cacheKey, UserResponse.class);
    if (cached != null) {
      return cached;
    }

    try {
      var response = users.findById(id)
        .map(UserResponse::fromEntity)
        .orElseThrow(() -> {
          log.warn("User not found with ID: {}", id);
          return new ResponseStatusException(HttpStatus.NOT_FOUND, "UserEntity not found");
        });

      redisCacheService.putObject(cacheKey, response, 120); // cache for 2 minutes
      return response;
    } catch (Exception e) {
      log.error("Error retrieving user {}: {}", id, e.getMessage());
      throw e;
    }
  }

  @Override
  @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
  public UserResponse getInternalById(UUID id) {
    log.info("Internal user retrieval for ID: {}", id);

    String cacheKey = "user:internal:" + id;
    UserResponse cached = redisCacheService.getObject(cacheKey, UserResponse.class);
    if (cached != null) {
      return cached;
    }

    try {
      var u = users.findById(id).orElseThrow(() -> {
        log.warn("User not found for internal request: {}", id);
        return new ResourceNotFoundException("UserEntity not found with ID: " + id);
      });

      var response = mapper.toDto(u);
      redisCacheService.putObject(cacheKey, response, 120); // cache for 2 minutes
      return response;
    } catch (Exception e) {
      log.error("Error in internal user retrieval for {}: {}", id, e.getMessage());
      throw e;
    }
  }

}
