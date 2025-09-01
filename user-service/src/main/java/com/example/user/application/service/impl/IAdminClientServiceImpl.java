package com.example.user.application.service.impl;

import com.example.user.application.service.IAdminClientService;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@AllArgsConstructor
public class IAdminClientServiceImpl implements IAdminClientService {
  private final WebClient adminWebClient;

  public String getAdminStatus() {
    return adminWebClient.get()
      .uri("/api/admin/health")
      .retrieve()
      .bodyToMono(String.class)
      .block();
  }
}
