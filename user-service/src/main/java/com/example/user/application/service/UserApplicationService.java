package com.example.user.application.service;

import com.example.user.application.dto.PageResponse;
import com.example.user.application.dto.UserResponseDto;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface UserApplicationService {
  PageResponse<UserResponseDto> list(Pageable pageable);
  UserResponseDto getById(UUID id);
  UserResponseDto getInternalById(UUID id);
}
