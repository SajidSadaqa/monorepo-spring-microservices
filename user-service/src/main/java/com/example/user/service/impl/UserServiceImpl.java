package com.example.user.service.impl;

import com.example.user.dto.PageResponse;
import com.example.user.dto.UserResponse;
import com.example.user.mapper.UserMapper;
import com.example.user.repository.UserRepository;
import com.example.user.service.UserService;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.user.exceptions.ResourceNotFoundException;



@Service
public class UserServiceImpl implements UserService {

  private final UserRepository users;
  private final UserMapper mapper;

  public UserServiceImpl(UserRepository users, UserMapper mapper) {
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
  // UserService
  public UserResponse getById(UUID id) {
    return users.findById(id)
      .map(UserResponse::fromEntity)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }



  @Override
  @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
  public UserResponse getInternalById(UUID id) {
    var u = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    return mapper.toDto(u);
  }

}
