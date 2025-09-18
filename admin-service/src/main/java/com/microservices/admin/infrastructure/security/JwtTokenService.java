package com.microservices.admin.infrastructure.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

/**
 * For simplicity in the admin-service (which mints S2S tokens), we use NimbusJwtEncoder with a
 * symmetric key in local profile and expect RSA in production via keystore config.
 * In production, wire JwtEncoder/Decoder beans from keystores; here we use Spring's auto where possible.
 */
@Component
@Primary
public class JwtTokenService {
  private final JwtEncoder encoder;
  private final long accessSeconds;

  public JwtTokenService(JwtEncoder encoder, @Value("${security.jwt.access-seconds:900}") long accessSeconds) {
    this.encoder = encoder;
    this.accessSeconds = accessSeconds;
  }

  public String mintAccess(String subject, Collection<? extends GrantedAuthority> auths, String issuer) {
    Instant now = Instant.now();

    // Strip ROLE_ prefix before putting in JWT
    List<String> roles = auths == null ? List.of() :
      auths.stream()
        .map(GrantedAuthority::getAuthority)                    // "ROLE_ADMIN"
        .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)   // → "ADMIN"
        .collect(Collectors.toList());

    var claims = JwtClaimsSet.builder()
      .subject(subject)
      .issuedAt(now)
      .expiresAt(now.plus(accessSeconds, ChronoUnit.SECONDS))
      .issuer(issuer)
      .claim("roles", roles)  // ["ADMIN"] - without prefix
      .build();

    return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String mintS2S(String subject, String issuer, String audience, List<String> authorities) {
    Instant now = Instant.now();

    // Strip ROLE_ prefix from authorities before putting in JWT
    List<String> roles = authorities == null ? List.of() :
      authorities.stream()
        .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)  // "ROLE_ADMIN" → "ADMIN"
        .collect(Collectors.toList());

    JwtClaimsSet claims = JwtClaimsSet.builder()
      .subject(subject)
      .issuer(issuer)
      .audience(List.of(audience))
      .issuedAt(now)
      .expiresAt(now.plus(accessSeconds, ChronoUnit.SECONDS))
      .claim("roles", roles)  // ["ADMIN"] - without prefix
      .build();

    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

}
