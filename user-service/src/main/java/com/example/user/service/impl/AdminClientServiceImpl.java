package com.example.user.service.impl;

import com.example.user.service.AdminClientService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@AllArgsConstructor
public class AdminClientServiceImpl implements AdminClientService {
  private final WebClient adminWebClient;

  public String getAdminStatus() {
    return adminWebClient.get()
      .uri("/api/admin/health")
      .retrieve()
      .bodyToMono(String.class)
      .block();
  }
}
