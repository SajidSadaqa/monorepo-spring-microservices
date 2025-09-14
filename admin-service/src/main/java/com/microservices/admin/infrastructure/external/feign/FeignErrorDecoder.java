package com.microservices.admin.infrastructure.external.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    return switch (response.status()) {
      case 400 -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request to user-service");
      case 401 -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized by user-service");
      case 403 -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden by user-service");
      case 404 -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
      default -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
        "Upstream error from user-service: " + response.status());
    };
  }
}
