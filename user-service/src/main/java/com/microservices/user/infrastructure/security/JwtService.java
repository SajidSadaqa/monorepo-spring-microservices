package com.microservices.user.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtService {

  // HS256 (local mint & verify)
  private final JwtEncoder accessEncoder;
  private final JwtDecoder hmacDecoder;

  // Optional RS256 (verify admin-service tokens)
  private final JwtDecoder rsaDecoder; // can be null if not configured

  private final long accessSeconds;
  private final long refreshSeconds;


  public JwtService(

    @Value("${security.jwt.access-seconds:900}") long accessSeconds,
    @Value("${security.jwt.refresh-seconds:604800}") long refreshSeconds,
    @Value("${security.jwt.hmac-secret:0123456789ABCDEF0123456789ABCDEF}") String secret,
    // Optional: classpath:/ file:/ or filesystem path to PEM public key
    @Value("${security.jwt.public-key:}") String publicKeyLocation
  ) {
    log.info("Initializing JWT Service with access token duration: {}s, refresh token duration: {}s",
      accessSeconds, refreshSeconds);


    this.accessSeconds = accessSeconds;
    this.refreshSeconds = refreshSeconds;


    // HS256 encoder/decoder (existing behavior)
    SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    this.accessEncoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(key));
    this.hmacDecoder = NimbusJwtDecoder
      .withSecretKey(key)
      .macAlgorithm(MacAlgorithm.HS256)
      .build();

    // Optional RS256 decoder (verify-only) for admin-service tokens
    this.rsaDecoder = loadRsaDecoder(publicKeyLocation);

    if (rsaDecoder != null) {
      log.info("RSA decoder initialized for external token verification");
    } else {
      log.info("RSA decoder not configured, using HMAC-only mode");
    }
  }

  public String createAccess(String subject, Collection<? extends GrantedAuthority> auths, String issuer, List<String> aud) {
    log.debug("Creating access token for subject: {} with roles: {}", subject,
      auths != null ? auths.stream().map(GrantedAuthority::getAuthority).toList() : "none");

    try {
      Instant now = Instant.now();
      JwtClaimsSet claims = JwtClaimsSet.builder()
        .subject(subject)
        .issuedAt(now)
        .expiresAt(now.plus(accessSeconds, ChronoUnit.SECONDS))
        .issuer(issuer)
        .claim("roleEntities", auths == null ? null : auths.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
        .audience(aud)
        .build();

      JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
      String token = accessEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();

      log.debug("Access token created successfully for subject: {}", subject);
      return token;
    } catch (Exception e) {
      log.error("Failed to create access token for subject {}: {}", subject, e.getMessage(), e);
      throw e;
    }
  }

  public String createRefresh(String subject, String issuer) {
    log.debug("Creating refresh token for subject: {}", subject);
    try {
      return createRefreshWithJti(subject, issuer, java.util.UUID.randomUUID().toString());
    } catch (Exception e) {
      log.error("Failed to create refresh token for subject {}: {}", subject, e.getMessage(), e);
      throw e;
    }
  }

  public String createRefreshWithJti(String subject, String issuer, String jti) {
    log.debug("Creating refresh token with JTI for subject: {}", subject);
    try {
      Instant now = Instant.now();
      JwtClaimsSet claims = JwtClaimsSet.builder()
        .subject(subject)
        .issuedAt(now)
        .expiresAt(now.plus(refreshSeconds, ChronoUnit.SECONDS))
        .issuer(issuer)
        .claim("typ", "refresh")
        .id(jti)
        .build();
      JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
      String token = accessEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
      log.debug("Refresh token created successfully for subject: {}", subject);
      return accessEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();

    } catch (Exception e) {
      log.error("Failed to create refresh token for subject {}: {}", subject, e.getMessage(), e);
      throw e;
    }
  }

  public Jwt decode(String token) {
    log.debug("Attempting to decode JWT token");

    try {
      // Try RS256 first
      if (rsaDecoder != null) {
        try {
          Jwt jwt = rsaDecoder.decode(token);
          log.debug("Token decoded successfully using RSA decoder");
          return jwt;
        } catch (RuntimeException e) {
          log.debug("RSA decode failed, trying HMAC: {}", e.getMessage());
        }
      }

      Jwt jwt = hmacDecoder.decode(token);
      log.debug("Token decoded successfully using HMAC decoder");
      return jwt;
    } catch (Exception e) {
      log.error("Token decode failed: {}", e.getMessage());
      throw e;
    }
  }

  // --- Helpers ---

  private JwtDecoder loadRsaDecoder(String location) {
    if (location == null || location.isBlank()) {
      return null;
    }
    try {
      String pem = readLocation(location);
      String base64 = pem.replaceAll("-----BEGIN (.*)-----", "")
        .replaceAll("-----END (.*)-----", "")
        .replaceAll("\\s", "");
      byte[] der = Base64.getDecoder().decode(base64);
      RSAPublicKey pk = (RSAPublicKey) KeyFactory.getInstance("RSA")
        .generatePublic(new X509EncodedKeySpec(der));
      return NimbusJwtDecoder.withPublicKey(pk).build();
    } catch (Exception e) {
      // If loading fails, ignore and rely on HS256 only
      return null;
    }
  }

  private String readLocation(String location) throws Exception {
    if (location.startsWith("classpath:")) {
      String path = location.substring("classpath:".length());
      ClassPathResource res = new ClassPathResource(path);
      try (InputStream in = res.getInputStream()) {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
      }
    }
    if (location.startsWith("file:")) {
      return Files.readString(Path.of(new java.net.URI(location)));
    }
    // Plain filesystem path
    return Files.readString(Path.of(location));
  }

  public String mintS2STokenForUserService() {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
      .issuer("admin-service")
      .subject("admin-service")
      .audience(List.of("user-service"))       // <-- REQUIRED for your filter
      .claim("s2s", true)                      // <-- REQUIRED for your filter
      .claim("scope", List.of("s2s"))          // optional, nice for @PreAuthorize
      .issuedAt(now)
      .expiresAt(now.plusSeconds(600))         // short-lived
      .build();

    JwsHeader jws = JwsHeader.with(MacAlgorithm.HS256).build();

    return this.accessEncoder.encode(JwtEncoderParameters.from(jws, claims)).getTokenValue();
  }
}
