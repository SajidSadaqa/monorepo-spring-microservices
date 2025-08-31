package com.example.user.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

  /**
   * HMAC key for HS256.
   * Tip: prefer a Base64 value in config and decode it here; length >= 32 bytes.
   */
  @Bean
  SecretKey hmacKey(
    @Value("${security.jwt.hmac-secret:0123456789ABCDEF0123456789ABCDEF}") String secret) {
    // If 'secret' is already raw bytes material, this is fine.
    // If you store Base64 in config, decode then pass bytes into SecretKeySpec.
    return new SecretKeySpec(secret.getBytes(), "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKey key) {
    return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(key));
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKey key) {
    // Decoder builder still supports withSecretKey(...)
    return NimbusJwtDecoder.withSecretKey(key)
      .macAlgorithm(MacAlgorithm.HS256)
      .build();
  }
}
