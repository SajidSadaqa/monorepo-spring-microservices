package com.example.admin.feign;

import com.example.admin.security.JwtService;
import feign.Client;
import feign.RequestInterceptor;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.List;
import javax.net.ssl.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S2SFeignConfig {

  @Value("${s2s.keystore.path:}")
  private String keystorePath;

  @Value("${s2s.keystore.password:}")
  private String keystorePassword;

  @Value("${s2s.truststore.path:}")
  private String truststorePath;

  @Value("${s2s.truststore.password:}")
  private String truststorePassword;

  @Value("${s2s.audience:user-service}")
  private String audience;

  private final JwtService jwtService;

  public S2SFeignConfig(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Bean
  public Client feignClient() throws Exception {
    // Build SSL context for mTLS
    KeyManager[] kms = null;
    if (keystorePath != null && !keystorePath.isBlank()) {
      KeyStore ks = KeyStore.getInstance("JKS");
      try (InputStream in = getClass().getClassLoader().getResourceAsStream(keystorePath)) {
        ks.load(in, keystorePassword.toCharArray());
      }
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(ks, keystorePassword.toCharArray());
      kms = kmf.getKeyManagers();
    }

    TrustManager[] tms = null;
    if (truststorePath != null && !truststorePath.isBlank()) {
      KeyStore ts = KeyStore.getInstance("JKS");
      try (InputStream in = getClass().getClassLoader().getResourceAsStream(truststorePath)) {
        ts.load(in, truststorePassword.toCharArray());
      }
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ts);
      tms = tmf.getTrustManagers();
    }

    SSLContext ctx = SSLContext.getInstance("TLS");
    ctx.init(kms, tms, new SecureRandom());
    SSLSocketFactory socketFactory = ctx.getSocketFactory();
    HostnameVerifier verifier = (host, session) -> true; // keep strict in prod

    return new feign.Client.Default(socketFactory, verifier);
  }

  @Bean
  public RequestInterceptor bearerInterceptor() {
    return template -> {
      String token = jwtService.mintS2S("admin-service", "admin-service", audience, List.of("ROLE_ADMIN"));
      template.header("Authorization", "Bearer " + token);
    };
  }
}
