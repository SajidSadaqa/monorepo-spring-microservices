package com.example.user.infrastructure.config;

import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class AdminWebClientConfig {

  @Value("classpath:tls/userEntity-truststore.p12")
  private Resource userTrustStore;

  @Value("classpath:tls/userEntity-keystore.p12")
  private Resource userKeyStore;

  @Bean
  public WebClient adminWebClient() throws Exception {
    // 1. Load truststore
    KeyStore trustStore = KeyStore.getInstance("PKCS12");
    try (InputStream is = userTrustStore.getInputStream()) {
      trustStore.load(is, "user123".toCharArray());
    }
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(trustStore);

    // 2. Load keystore
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    try (InputStream is = userKeyStore.getInputStream()) {
      keyStore.load(is, "user123".toCharArray());
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(keyStore, "user123".toCharArray());

    // 3. Build SSL context
    var sslContext = SslContextBuilder.forClient()
      .keyManager(kmf)
      .trustManager(tmf)
      .build();

    HttpClient httpClient = HttpClient.create()
      .secure(ssl -> ssl.sslContext(sslContext));

    // 4. Build WebClient
    return WebClient.builder()
      .baseUrl("https://localhost:8445")
      .clientConnector(new ReactorClientHttpConnector(httpClient))
      .build();
  }
}
