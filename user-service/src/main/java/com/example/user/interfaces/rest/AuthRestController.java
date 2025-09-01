package com.example.user.interfaces.rest;

import com.example.user.application.dto.AuthReq;
import com.example.user.application.dto.SignupReq;
import com.example.user.application.dto.TokenResponse;
import com.example.user.application.service.IAuthApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI; // <-- add this import

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthRestController {

  private final IAuthApplicationService auth;

  public AuthRestController(IAuthApplicationService auth) { this.auth = auth; }

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
