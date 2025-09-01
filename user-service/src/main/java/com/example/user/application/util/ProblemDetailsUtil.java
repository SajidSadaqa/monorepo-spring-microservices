package com.example.user.application.util;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

public final class ProblemDetailsUtil {
  private ProblemDetailsUtil() {}

  public static ProblemDetail pd(int status, String code, String detail, WebRequest req) {
    ProblemDetail pd = ProblemDetail.forStatus(status);
    pd.setTitle(code);
    pd.setDetail(detail);
    pd.setType(URI.create("about:blank"));
    pd.setProperty("timestamp", Instant.now().toString());
    pd.setProperty("path", req.getDescription(false).replace("uri=", ""));
    pd.setProperty("code", code);
    return pd;
  }
}
