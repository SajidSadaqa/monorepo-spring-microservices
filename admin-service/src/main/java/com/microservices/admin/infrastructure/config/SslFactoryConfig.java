package com.microservices.admin.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class SslFactoryConfig {

  @Value("${s2s.truststore.path}")
  private Resource trustStoreResource;

  @Value("${s2s.truststore.password}")
  private String trustStorePassword;

  @Value("${s2s.keystore.path}")
  private Resource keyStoreResource;

  @Value("${s2s.keystore.password}")
  private String keyStorePassword;

  @Bean
  public TrustManagerFactory trustManagerFactory() throws Exception {
    KeyStore ts = KeyStore.getInstance("PKCS12");
    try (InputStream is = trustStoreResource.getInputStream()) {
      ts.load(is, trustStorePassword.toCharArray());
    }
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ts);
    return tmf;
  }

  @Bean
  public KeyManagerFactory keyManagerFactory() throws Exception {
    KeyStore ks = KeyStore.getInstance("PKCS12");
    try (InputStream is = keyStoreResource.getInputStream()) {
      ks.load(is, keyStorePassword.toCharArray());
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    // The keystore password is used to unlock the keystore file.
    // The key password is used to unlock the private key within the keystore.
    // They are often the same but can be different.
    kmf.init(ks, keyStorePassword.toCharArray());
    return kmf;
  }
}
