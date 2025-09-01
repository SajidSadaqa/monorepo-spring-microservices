package com.example.user.application.service.impl;

import com.example.user.application.mapper.IUserMapper;
import com.example.user.application.service.IUserApplicationService;
import com.example.user.application.dto.PageResponse;
import com.example.user.application.dto.UserResponseDto;
import com.example.user.infrastructure.persistence.UserJpaRepository;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.user.interfaces.exception.ResourceNotFoundException;



@Service
public class IUserApplicationServiceImpl implements IUserApplicationService {

  private final UserJpaRepository users;
  private final IUserMapper mapper;

  public IUserApplicationServiceImpl(UserJpaRepository users, IUserMapper mapper) {
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
  // IUserApplicationService
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
