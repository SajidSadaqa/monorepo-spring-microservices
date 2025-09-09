package com.example.user.interfaces.rest;

import com.example.user.application.dto.AuthReq;
import com.example.user.application.dto.SignupReq;
import com.example.user.application.dto.TokenResponse;
import com.example.user.application.service.AuthApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI; // <-- add this import

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
@Slf4j
@AllArgsConstructor

public class AuthRestController {

  private final AuthApplicationService auth;

  @PostMapping("/signup")
  public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupReq req) {
    log.info("Signup request received for username: {}", req.username());

    try {
      TokenResponse tokens = auth.signup(req);
      log.info("Signup successful for username: {}", req.username());
      return ResponseEntity
        .created(URI.create("/api/users/" + req.username()))
        .body(tokens);
    } catch (Exception e) {
      log.error("Signup failed for username {}: {}", req.username(), e.getMessage());
      throw e;
    }
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthReq req) {
    log.info("Login request received for username: {}", req.username());

    try {
      TokenResponse response = auth.login(req.username(), req.password());
      log.info("Login successful for username: {}", req.username());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Login failed for username {}: {}", req.username(), e.getMessage());
      throw e;
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    log.info("Token refresh request received");

    try {
      String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : "";
      TokenResponse response = auth.refresh(token);
      log.info("Token refresh successful");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Token refresh failed: {}", e.getMessage());
      throw e;
    }
  }
}
