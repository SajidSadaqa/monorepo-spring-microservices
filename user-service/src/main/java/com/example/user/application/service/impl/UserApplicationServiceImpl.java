package com.example.user.application.service.impl;

import com.example.user.application.service.UserApplicationService;
import com.example.user.application.dto.PageResponse;
import com.example.user.application.dto.UserResponseDto;
import com.example.user.application.mapper.UserMapper;
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
  private final UserMapper mapper;

  public UserApplicationServiceImpl(UserJpaRepository users, UserMapper mapper) {
    this.users = users;
    this.mapper = mapper;
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public PageResponse<UserResponseDto> list(Pageable pageable) {
    var page = users.findAll(pageable).map(mapper::toDto);
    return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

  @Override
  // UserApplicationService
  public UserResponseDto getById(UUID id) {
    return users.findById(id)
      .map(UserResponseDto::fromEntity)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }



  @Override
  @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
  public UserResponseDto getInternalById(UUID id) {
    var u = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    return mapper.toDto(u);
  }

}
