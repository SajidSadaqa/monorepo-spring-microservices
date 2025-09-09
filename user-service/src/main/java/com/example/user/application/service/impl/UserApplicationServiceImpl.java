package com.example.user.application.service.impl;

import com.example.user.application.dto.UserResponse;
import com.example.user.application.mapper.IUserMapper;
import com.example.user.application.service.UserApplicationService;
import com.example.user.application.dto.PageResponse;
import com.example.user.infrastructure.persistence.UserJpaRepository;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.user.interfaces.exception.ResourceNotFoundException;



@Service
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
    var page = users.findAll(pageable).map(mapper::toDto);
    return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

  @Override
  // UserApplicationService
  public UserResponse getById(UUID id) {
    return users.findById(id)
      .map(UserResponse::fromEntity)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UserEntity not found"));
  }



  @Override
  @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
  public UserResponse getInternalById(UUID id) {
    var u = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("UserEntity not found with ID: " + id));
    return mapper.toDto(u);
  }

}
