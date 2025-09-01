package com.example.user.config;

import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

  @Value("${security.jwt.public-key}")
  private Resource publicKeyPem;

  private String readKey(Resource resource) throws Exception {
    String key = Files.readString(resource.getFile().toPath());
    return key.replaceAll("-----BEGIN (.*)-----", "")
      .replaceAll("-----END (.*)-----", "")
      .replaceAll("\\s", "");
  }

  @Bean
  public RSAPublicKey rsaPublicKey() throws Exception {
    byte[] decoded = Base64.getDecoder().decode(readKey(publicKeyPem));
    return (RSAPublicKey) KeyFactory.getInstance("RSA")
      .generatePublic(new X509EncodedKeySpec(decoded));
  }

  @Bean
  public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
    return NimbusJwtDecoder.withPublicKey(publicKey).build();
  }
}
