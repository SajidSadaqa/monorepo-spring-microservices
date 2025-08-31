package com.example.admin.util;

import java.net.URI;
import java.time.Instant;
import org.springframework.http.ProblemDetail;
import org.springframework.web.context.request.WebRequest;

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
