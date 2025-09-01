package com.example.admin.config;

import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

@Configuration
public class UserWebClientConfig {

  @Bean
  public WebClient userWebClient(
    @Value("${s2s.url}" ) String userServiceUrl,
    KeyManagerFactory kmf, // Inject the KeyManagerFactory bean
    TrustManagerFactory tmf // Inject the TrustManagerFactory bean
  ) throws SSLException {
    // Use the factories to build the Netty-specific SslContext
    io.netty.handler.ssl.SslContext nettySslContext = SslContextBuilder.forClient()
      .trustManager(tmf)
      .keyManager(kmf)
      .build();

    HttpClient httpClient = HttpClient.create( )
      .secure(ssl -> ssl.sslContext(nettySslContext));

    return WebClient.builder()
      .baseUrl(userServiceUrl)
      .clientConnector(new ReactorClientHttpConnector(httpClient ))
      .build();
  }
}
