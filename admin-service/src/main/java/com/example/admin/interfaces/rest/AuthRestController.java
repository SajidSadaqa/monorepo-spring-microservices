package com.example.admin.interfaces.rest;

import com.example.admin.application.dto.request.AdminLoginReqDto;
import com.example.admin.application.dto.response.TokenResDto;
import com.example.admin.application.service.AdminAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@Tag(name = "Admin Authentication")
public class AuthRestController {

  private final AdminAuthService auth;

  public AuthRestController(AdminAuthService auth) { this.auth = auth; }

  @PostMapping("/login")
  public ResponseEntity<TokenResDto> login(@Valid @RequestBody AdminLoginReqDto req) {
    return ResponseEntity.ok(auth.login(req.username(), req.password()));
  }

}

