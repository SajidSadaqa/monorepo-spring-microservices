package com.example.user.application.service.impl;

import com.example.user.application.dto.UserResponse;
import com.example.user.application.mapper.IUserMapper;
import com.example.user.application.service.UserApplicationService;
import com.example.user.application.dto.PageResponse;
import com.example.user.infrastructure.persistence.UserJpaRepository;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.user.interfaces.exception.ResourceNotFoundException;



@Service
@Slf4j
public class UserApplicationServiceImpl implements UserApplicationService {

  private final UserJpaRepository users;
  private final IUserMapper mapper;

  public UserApplicationServiceImpl(UserJpaRepository users, IUserMapper mapper) {
    this.users = users;
    this.mapper = mapper;
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public PageResponse<UserResponse> list(Pageable pageable) {
    log.info("Admin requesting user list - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

    try {
      var page = users.findAll(pageable).map(mapper::toDto);
      log.info("Retrieved {} users out of {} total", page.getNumberOfElements(), page.getTotalElements());
      return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    } catch (Exception e) {
      log.error("Error retrieving user list: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public UserResponse getById(UUID id) {
    log.info("Retrieving user by ID: {}", id);

    try {
      var response = users.findById(id)
        .map(UserResponse::fromEntity)
        .orElseThrow(() -> {
          log.warn("User not found with ID: {}", id);
          return new ResponseStatusException(HttpStatus.NOT_FOUND, "UserEntity not found");
        });

      log.debug("User retrieved successfully: {}", id);
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

    try {
      var u = users.findById(id).orElseThrow(() -> {
        log.warn("User not found for internal request: {}", id);
        return new ResourceNotFoundException("UserEntity not found with ID: " + id);
      });

      var response = mapper.toDto(u);
      log.debug("Internal user data retrieved for ID: {}", id);
      return response;
    } catch (Exception e) {
      log.error("Error in internal user retrieval for {}: {}", id, e.getMessage());
      throw e;
    }
  }

}
