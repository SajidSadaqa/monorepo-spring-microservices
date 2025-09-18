package com.microservices.admin.interfaces.rest;

import com.microservices.admin.application.dto.request.AdminLoginReqDto;
import com.microservices.admin.application.dto.response.TokenResDto;
import com.microservices.admin.application.service.AdminAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@Tag(name = "Admin Authentication")
public class AuthRestController {

  private static final Logger logger = LoggerFactory.getLogger(AuthRestController.class);
  private final AdminAuthService auth;

  public AuthRestController(AdminAuthService auth) {
    this.auth = auth;
    logger.info("AuthRestController initialized");
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResDto> login(@Valid @RequestBody AdminLoginReqDto req) {
    logger.info("🚀 LOGIN REQUEST RECEIVED - Username: {}", req.username());
    logger.debug("Request details - Username: {}, Password length: {}", req.username(),
      req.password() != null ? req.password().length() : 0);

    try {
      TokenResDto result = auth.login(req.username(), req.password());
      logger.info("✅ LOGIN SUCCESS - Username: {}", req.username());
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      logger.error("❌ LOGIN FAILED - Username: {}, Error: {}", req.username(), e.getMessage(), e);
      throw e;
    }
  }
}
