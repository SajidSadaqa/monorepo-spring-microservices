package com.example.admin.service.impl;

import com.example.admin.service.UserClientService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@AllArgsConstructor
public class UserClientServiceImpl implements UserClientService {

  private final WebClient userWebClient;

  public String callUserHealth() {
    return userWebClient.get()
      .uri("/api/user/health")
      .retrieve()
      .bodyToMono(String.class)
      .block();
  }
}
