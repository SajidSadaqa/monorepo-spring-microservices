package com.example.user.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI openAPI() {
    final String scheme = "bearerAuth";
    return new OpenAPI()
      .info(new Info().title("UserEntity Service API").version("v1"))
      .components(
        new Components()
          .addSecuritySchemes(
            scheme,
            new SecurityScheme()
              .name(scheme)
              .type(SecurityScheme.Type.HTTP)
              .scheme("bearer")
              .bearerFormat("JWT")))
      .addSecurityItem(new SecurityRequirement().addList(scheme));
  }
}
