package com.example.user.controller;

import com.example.user.dto.AuthReq;
import com.example.user.dto.SignupReq;
import com.example.user.dto.TokenResponse;
import com.example.user.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI; // <-- add this import

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

  private final AuthService auth;

  public AuthController(AuthService auth) { this.auth = auth; }

  @PostMapping("/signup")
  public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupReq req) {
    TokenResponse tokens = auth.signup(req);
    // If you don’t have a user resource route, you can use URI.create("/api/auth/signup")
    return ResponseEntity
      .created(URI.create("/api/users/" + req.username())) // 201 + Location header
      .body(tokens);
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthReq req) {
    return ResponseEntity.ok(auth.login(req.username(), req.password()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : "";
    return ResponseEntity.ok(auth.refresh(token));
  }
}
