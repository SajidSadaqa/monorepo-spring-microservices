package com.example.admin.feign;

import com.example.admin.security.JwtService;
import feign.Client;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.util.List;

@Configuration
public class S2SFeignConfig {

  private final JwtService jwtService;
  private final String audience;

  public S2SFeignConfig(JwtService jwtService, @Value("${s2s.audience}") String audience) {
    this.jwtService = jwtService;
    this.audience = audience;
  }

  @Bean
  public Client feignClient(KeyManagerFactory kmf, TrustManagerFactory tmf) throws Exception {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    // Use the factories to get the managers and initialize the context
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    HostnameVerifier verifier = (hostname, session) -> true; // OK for dev, use strict for prod
    return new Client.Default(sslContext.getSocketFactory(), verifier);
  }

  @Bean
  public RequestInterceptor bearerInterceptor() {
    return template -> {
      String token = jwtService.mintS2S("admin-service", "admin-service", audience, List.of("ROLE_ADMIN"));
      template.header("Authorization", "Bearer " + token);
    };
  }
}
