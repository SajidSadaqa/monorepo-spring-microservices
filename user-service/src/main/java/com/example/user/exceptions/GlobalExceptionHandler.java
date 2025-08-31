package com.example.user.exceptions;

import com.example.user.util.ProblemDetailsUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global error handler producing RFC-7807 ProblemDetails.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final MessageSource ms;
  private final Environment env;

  public GlobalExceptionHandler(MessageSource ms, Environment env) {
    this.ms = ms;
    this.env = env;
  }

  // --- Validation & binding ---

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex,
                                                        WebRequest req, Locale locale, HttpServletRequest http) {
    String detail = ex.getBindingResult().getFieldErrors().stream()
      .map(fe -> fe.getField() + ": " + resolve(fe.getDefaultMessage(), locale))
      .collect(Collectors.joining("; "));
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", detail, req, http);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ProblemDetail> handleBind(BindException ex,
                                                  WebRequest req, Locale locale, HttpServletRequest http) {
    String detail = ex.getBindingResult().getFieldErrors().stream()
      .map(fe -> fe.getField() + ": " + resolve(fe.getDefaultMessage(), locale))
      .collect(Collectors.joining("; "));
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", detail, req, http);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraint(ConstraintViolationException ex,
                                                        WebRequest req, Locale locale, HttpServletRequest http) {
    // ex.getMessage() is already human-readable like "field: must not be blank"
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), req, http);
  }

  // --- Common web errors ---

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleBadJson(HttpMessageNotReadableException ex,
                                                     WebRequest req, Locale locale, HttpServletRequest http) {
    return build(HttpStatus.BAD_REQUEST, "MALFORMED_JSON",
      resolve("error.malformed.json", locale), req, http, Map.of("cause", root(ex)));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ProblemDetail> handleMissingParam(MissingServletRequestParameterException ex,
                                                          WebRequest req, Locale locale, HttpServletRequest http) {
    String detail = resolve("error.missing.param", locale) + ": " + ex.getParameterName();
    return build(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", detail, req, http);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                          WebRequest req, Locale locale, HttpServletRequest http) {
    String detail = resolve("error.type.mismatch", locale) + ": " + ex.getName();
    return build(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", detail, req, http);
  }

  // --- Domain & security ---

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex,
                                                      WebRequest req, HttpServletRequest http) {
    return build(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_ERROR", ex.getMessage(), req, http);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleDenied(AccessDeniedException ex,
                                                    WebRequest req, Locale locale, HttpServletRequest http) {
    return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", resolve("access.denied", locale), req, http);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ProblemDetail> handleBadCreds(BadCredentialsException ex,
                                                      WebRequest req, Locale locale, HttpServletRequest http) {
    return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", resolve("auth.login.invalid", locale), req, http);
  }

  // --- Fallback ---

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleAny(Exception ex, WebRequest req, HttpServletRequest http, Locale locale) {
    // Structured log with request method+path; stacktrace logged server-side only
    log.error("Unhandled exception on {} {} - {}", http.getMethod(), http.getRequestURI(), ex.toString(), ex);

    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle("INTERNAL_ERROR");
    pd.setDetail(resolve("error.internal", locale));
    enrich(pd, req, http, Map.of("exception", ex.getClass().getName()));

    // Expose debug fields only on non-prod profiles
    if (env.acceptsProfiles(Profiles.of("dev", "test"))) {
      pd.setProperty("message", ex.getMessage());
      pd.setProperty("stacktrace", org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(ex));
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
  }

  // --- Helpers ---

  private ResponseEntity<ProblemDetail> build(HttpStatus status, String title, String detail,
                                              WebRequest req, HttpServletRequest http) {
    return build(status, title, detail, req, http, Map.of());
  }

  private ResponseEntity<ProblemDetail> build(HttpStatus status, String title, String detail,
                                              WebRequest req, HttpServletRequest http,
                                              Map<String, Object> extraProps) {
    ProblemDetail pd = ProblemDetailsUtil.pd(status.value(), title, detail, req);
    enrich(pd, req, http, extraProps);
    return ResponseEntity.status(status).body(pd);
  }

  private void enrich(ProblemDetail pd, WebRequest req, HttpServletRequest http, Map<String, Object> extraProps) {
    pd.setProperty("path", http.getRequestURI());
    pd.setProperty("timestamp", OffsetDateTime.now().toString());
    String requestId = http.getHeader("X-Request-Id");
    if (requestId != null && !requestId.isBlank()) {
      pd.setProperty("requestId", requestId);
    }
    extraProps.forEach(pd::setProperty);
  }

  private String resolve(String keyOrMsg, Locale locale) {
    try {
      return ms.getMessage(keyOrMsg, null, locale);
    } catch (Exception ignore) {
      return keyOrMsg; // treat as literal if no bundle key
    }
  }

  private static String root(Throwable t) {
    Throwable r = t;
    while (r.getCause() != null) r = r.getCause();
    return r.getClass().getSimpleName();
  }
}
