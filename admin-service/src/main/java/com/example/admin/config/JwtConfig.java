package com.example.admin.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.*;

import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

  @Value("${security.jwt.public-key}")
  private Resource publicKeyPem;

  @Value("${security.jwt.private-key}")
  private Resource privateKeyPem;

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
  public RSAPrivateKey rsaPrivateKey() throws Exception {
    byte[] decoded = Base64.getDecoder().decode(readKey(privateKeyPem));
    return (RSAPrivateKey) KeyFactory.getInstance("RSA")
      .generatePrivate(new PKCS8EncodedKeySpec(decoded));
  }

  @Bean
  public JWKSource<SecurityContext> jwkSource(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
    RSAKey rsaKey = new RSAKey.Builder(publicKey)
      .privateKey(privateKey)
      .keyID("admin-key-id")
      .build();
    return new ImmutableJWKSet<>(new JWKSet(rsaKey));
  }

  @Bean
  public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
    return NimbusJwtDecoder.withPublicKey(publicKey).build();
  }
}
