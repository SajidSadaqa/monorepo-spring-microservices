package com.example.admin.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

/**
 * For simplicity in the admin-service (which mints S2S tokens), we use NimbusJwtEncoder with a
 * symmetric key in local profile and expect RSA in production via keystore config.
 * In production, wire JwtEncoder/Decoder beans from keystores; here we use Spring's auto where possible.
 */
@Component
@Primary
public class JwtService {
  private final JwtEncoder encoder;
  private final long accessSeconds;

  public JwtService(JwtEncoder encoder, @Value("${security.jwt.access-seconds:900}") long accessSeconds) {
    this.encoder = encoder;
    this.accessSeconds = accessSeconds;
  }

  public String mintAccess(String subject, Collection<? extends GrantedAuthority> auths, String issuer) {
    Instant now = Instant.now();
    var claims =
      JwtClaimsSet.builder()
        .subject(subject)
        .issuedAt(now)
        .expiresAt(now.plus(accessSeconds, ChronoUnit.SECONDS))
        .issuer(issuer)
        .claim(
          "roles",
          auths == null
            ? null
            : auths.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
        .build();
    return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String mintS2S(String subject, String issuer, String audience, Collection<String> roles) {
    Instant now = Instant.now();
    var claims =
      JwtClaimsSet.builder()
        .subject(subject)
        .issuedAt(now)
        .expiresAt(now.plus(accessSeconds, ChronoUnit.SECONDS))
        .issuer(issuer)
        .audience(java.util.List.of(audience))
        .claim("roles", roles)
        .claim("typ", "s2s")
        .build();
    return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
}
