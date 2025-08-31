package com.example.user.service;

import com.example.user.dto.PageResponse;
import com.example.user.dto.UserResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface UserService {
  PageResponse<UserResponse> list(Pageable pageable);
  UserResponse getById(UUID id);
  UserResponse getInternalById(UUID id);
}
