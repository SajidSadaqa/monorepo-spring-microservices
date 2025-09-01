package com.example.admin.infrastructure.external;

import com.example.admin.application.dto.response.PageResDto;
import com.example.admin.application.dto.response.UserResDto;
import java.util.UUID;

import com.example.admin.infrastructure.external.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
  name = "user-service",
  url = "${s2s.url}",
  configuration = FeignClientConfig.class
)
public interface UserDirectoryClient {

  @GetMapping("/api/users/{id}")
  UserResDto getUserById(@PathVariable("id") UUID id);

  @GetMapping("/api/users")
  PageResDto<UserResDto> listUsers(@RequestParam int page,
                                   @RequestParam int size,
                                   @RequestParam(defaultValue = "createdAt,desc") String sort);
}

