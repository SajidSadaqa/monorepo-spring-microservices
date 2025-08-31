package com.example.user.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

  private final JwtEncoder accessEncoder;
  private final JwtDecoder accessDecoder;
  private final long accessSeconds;
  private final long refreshSeconds;

  public JwtService(
    @Value("${security.jwt.access-seconds:900}") long accessSeconds,
    @Value("${security.jwt.refresh-seconds:604800}") long refreshSeconds,
    @Value("${security.jwt.hmac-secret:0123456789ABCDEF0123456789ABCDEF}") String secret
    // Prefer loading this from config; must be >= 32 bytes for HS256
  ) {
    this.accessSeconds = accessSeconds;
    this.refreshSeconds = refreshSeconds;

    // Build HMAC key for HS256
    SecretKey key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

    // Encoder (Spring Security 6 style)
    this.accessEncoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(key));

    // Decoder
    this.accessDecoder = NimbusJwtDecoder
      .withSecretKey(key)
      .macAlgorithm(MacAlgorithm.HS256)
      .build();
  }

  public String createAccess(
    String subject,
    Collection<? extends GrantedAuthority> auths,
    String issuer,
    List<String> aud) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
      .subject(subject)
      .issuedAt(now)
      .expiresAt(now.plus(accessSeconds, ChronoUnit.SECONDS))
      .issuer(issuer)
      .claim("roles", auths == null
        ? null
        : auths.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
      .audience(aud)
      .build();

    var headers = org.springframework.security.oauth2.jwt.JwsHeader.with(MacAlgorithm.HS256).build();
    return accessEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
  }

  public String createRefresh(String subject, String issuer) {
    return createRefreshWithJti(subject, issuer, java.util.UUID.randomUUID().toString());
  }

  public String createRefreshWithJti(String subject, String issuer, String jti) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
      .subject(subject)
      .issuedAt(now)
      .expiresAt(now.plus(refreshSeconds, ChronoUnit.SECONDS))
      .issuer(issuer)
      .claim("typ", "refresh")
      .id(jti)
      .build();

    var headers = org.springframework.security.oauth2.jwt.JwsHeader.with(MacAlgorithm.HS256).build();
    return accessEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
  }

  public Jwt decode(String token) {
    return accessDecoder.decode(token);
  }
}
