package com.example.admin.config;

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
public class UserWebClientConfig {

  @Value("${user-service.url}")
  private String userServiceUrl;

  @Value("${user-service.trust-store}")
  private Resource trustStore;

  @Value("${user-service.trust-store-password}")
  private String trustStorePassword;

  @Value("${user-service.key-store}")
  private Resource keyStore;

  @Value("${user-service.key-store-password}")
  private String keyStorePassword;

  @Value("${user-service.key-password}")
  private String keyPassword;

  @Bean
  public WebClient userWebClient() throws Exception {
    // Load truststore
    KeyStore ts = KeyStore.getInstance("PKCS12");
    try (InputStream is = trustStore.getInputStream()) {
      ts.load(is, trustStorePassword.toCharArray());
    }
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ts);

    // Load keystore
    KeyStore ks = KeyStore.getInstance("PKCS12");
    try (InputStream is = keyStore.getInputStream()) {
      ks.load(is, keyStorePassword.toCharArray());
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, keyPassword.toCharArray());

    // Build SSL context
    var sslContext = SslContextBuilder.forClient()
      .trustManager(tmf)
      .keyManager(kmf)
      .build();

    HttpClient httpClient = HttpClient.create()
      .secure(ssl -> ssl.sslContext(sslContext));

    return WebClient.builder()
      .baseUrl(userServiceUrl)
      .clientConnector(new ReactorClientHttpConnector(httpClient))
      .build();
  }
}
