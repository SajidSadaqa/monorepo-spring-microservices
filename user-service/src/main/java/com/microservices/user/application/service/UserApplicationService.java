package com.microservices.user.application.service;

import com.microservices.user.application.dto.PageResponse;
import com.microservices.user.application.dto.UserResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface UserApplicationService {
  PageResponse<UserResponse> list(Pageable pageable);
  UserResponse getById(UUID id);
  UserResponse getInternalById(UUID id);
}
