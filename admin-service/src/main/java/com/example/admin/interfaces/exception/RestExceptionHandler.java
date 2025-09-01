package com.example.admin.interfaces.exception;

import com.example.admin.application.util.ProblemDetailsUtil;
import jakarta.validation.ConstraintViolationException;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class RestExceptionHandler {
  private final MessageSource ms;

  public RestExceptionHandler(MessageSource ms) {
    this.ms = ms;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest req, Locale locale) {
    String detail =
      ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + resolve(fe.getDefaultMessage(), locale))
        .collect(Collectors.joining("; "));
    return ProblemDetailsUtil.pd(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", detail, req);
  }

  @ExceptionHandler(BindException.class)
  public ProblemDetail handleBind(BindException ex, WebRequest req, Locale locale) {
    String detail =
      ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + resolve(fe.getDefaultMessage(), locale))
        .collect(Collectors.joining("; "));
    return ProblemDetailsUtil.pd(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", detail, req);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraint(ConstraintViolationException ex, WebRequest req, Locale locale) {
    return ProblemDetailsUtil.pd(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", ex.getMessage(), req);
  }

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusiness(BusinessException ex, WebRequest req) {
    return ProblemDetailsUtil.pd(422, "BUSINESS_ERROR", ex.getMessage(), req);
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ProblemDetail handleDenied(org.springframework.security.access.AccessDeniedException ex, WebRequest req, Locale locale) {
    return ProblemDetailsUtil.pd(403, "ACCESS_DENIED", resolve("access.denied", locale), req);
  }

  @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
  public ProblemDetail handleBadCreds(Exception ex, WebRequest req, Locale locale) {
    return ProblemDetailsUtil.pd(401, "UNAUTHORIZED", resolve("auth.login.invalid", locale), req);
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleAll(Exception ex, WebRequest req, Locale locale) {
        // Preserve status codes already expressed via ResponseStatusException
          if (ex instanceof ResponseStatusException rse) {
            throw rse; // let Spring map it to the correct HTTP status / ProblemDetail
          }    // Optional: also unwrap nested causes
          Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof ResponseStatusException rse2) {
                throw rse2;
              }
            cause = cause.getCause();
          }
    return ProblemDetailsUtil.pd(500, "INTERNAL_ERROR", resolve("error.internal", locale), req);
  }

  // Preserve HTTP status codes coming from downstream (e.g., Feign -> ResponseStatusException)
      @ExceptionHandler(ResponseStatusException.class)
  public ProblemDetail handleResponseStatus(ResponseStatusException ex, WebRequest req, Locale locale) {
        int status = ex.getStatusCode().value();
        HttpStatus hs = HttpStatus.resolve(status);
        String code = (hs != null ? hs.name() : ("HTTP_" + status));
        String detail = (ex.getReason() != null ? ex.getReason() : resolve("error.internal", locale));
        return ProblemDetailsUtil.pd(status, code, detail, req);
      }

  private String resolve(String keyOrMsg, Locale locale) {
    try {
      return ms.getMessage(keyOrMsg, null, locale);
    } catch (Exception ignore) {
      return keyOrMsg;
    }
  }
}
