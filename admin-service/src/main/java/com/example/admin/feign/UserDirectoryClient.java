package com.example.admin.feign;

import com.example.admin.dto.PageResponse;
import com.example.admin.dto.UserResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
  name = "user-service",
  url = "${user-service.url}",
  configuration = S2SFeignConfig.class
)
public interface UserDirectoryClient {

  @GetMapping("/api/users/{id}")
  UserResponse getUserById(@PathVariable("id") UUID id);

  @GetMapping("/api/users")
  PageResponse<UserResponse> listUsers(@RequestParam int page,
                                       @RequestParam int size,
                                       @RequestParam(defaultValue = "createdAt,desc") String sort);
}

