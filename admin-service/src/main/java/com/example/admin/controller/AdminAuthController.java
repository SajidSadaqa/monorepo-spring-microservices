package com.example.admin.controller;

import com.example.admin.dto.AdminLoginReq;
import com.example.admin.dto.TokenResponse;
import com.example.admin.service.AdminAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Authentication")
public class AdminAuthController {

  private final AdminAuthService auth;

  public AdminAuthController(AdminAuthService auth) { this.auth = auth; }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody AdminLoginReq req) {
    return ResponseEntity.ok(auth.login(req.username(), req.password()));
  }
}
