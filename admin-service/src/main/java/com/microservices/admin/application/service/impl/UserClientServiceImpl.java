package com.microservices.admin.application.service.impl;

import com.microservices.admin.application.service.UserClientService;
import com.microservices.admin.application.util.RedisCacheService;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@AllArgsConstructor
public class UserClientServiceImpl implements UserClientService {

  private final WebClient userWebClient;
  private final RedisCacheService redisCacheService;

  public String callUserHealth() {
    String cacheKey = "user:health";
    String cached = redisCacheService.getValue(cacheKey, String.class);
    if (cached != null) {
      return cached;
    }

    String status = userWebClient.get()
      .uri("/api/user/health")
      .retrieve()
      .bodyToMono(String.class)
      .block();

    redisCacheService.putValue(cacheKey, status, 30); // cache health check for 30s
    return status;
  }
}
