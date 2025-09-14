package com.microservices.user.application.service.impl;

import com.microservices.user.application.service.AdminClientService;
import com.microservices.user.application.util.RedisCacheService;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@AllArgsConstructor
public class AdminClientServiceImpl implements AdminClientService {
  private final WebClient adminWebClient;
  private final RedisCacheService redisCacheService;

  public String getAdminStatus() {
    // Example: cache health status for 30s
    String cacheKey = "admin:health";
    String cached = redisCacheService.getValue(cacheKey, String.class);
    if (cached != null) {
      return cached;
    }

    String status = adminWebClient.get()
      .uri("/api/admin/health")
      .retrieve()
      .bodyToMono(String.class)
      .block();

    redisCacheService.putValue(cacheKey, status, 30);
    return status;
  }
}
