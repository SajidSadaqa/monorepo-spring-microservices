package com.example.user.interfaces.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

  private RestExceptionHandler exceptionHandler;
  private StaticMessageSource messageSource;
  private Environment environment;

  @BeforeEach
  void setUp() {
    messageSource = new StaticMessageSource();
    environment = new MockEnvironment();
    exceptionHandler = new RestExceptionHandler(messageSource, environment);

    // Setup common messages
    messageSource.addMessage("auth.login.invalid", Locale.ENGLISH, "Invalid credentials");
    messageSource.addMessage("access.denied", Locale.ENGLISH, "Access denied");
  }

  @Test
  void handleBusiness_ShouldReturnUnprocessableEntity() {
    // Given
    BusinessException ex = new BusinessException("USER_EXISTS", "Username already exists");
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/signup");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    // When
    ResponseEntity<ProblemDetail> response = exceptionHandler.handleBusiness(ex, webRequest, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("BUSINESS_ERROR");
    assertThat(response.getBody().getDetail()).isEqualTo("Username already exists");
  }

  @Test
  void handleBadCreds_ShouldReturnUnauthorized() {
    // Given
    BadCredentialsException ex = new BadCredentialsException("Invalid credentials");
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    // When
    ResponseEntity<ProblemDetail> response = exceptionHandler.handleBadCreds(
      ex, webRequest, Locale.ENGLISH, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("UNAUTHORIZED");
    assertThat(response.getBody().getDetail()).isEqualTo("Invalid credentials");
  }

  @Test
  void handleDenied_ShouldReturnForbidden() {
    // Given
    AccessDeniedException ex = new AccessDeniedException("Access denied");
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    // When
    ResponseEntity<ProblemDetail> response = exceptionHandler.handleDenied(
      ex, webRequest, Locale.ENGLISH, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("ACCESS_DENIED");
  }

  @Test
  void handleValidation_ShouldReturnBadRequest() {
    // Given
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
    bindingResult.addError(new FieldError("target", "username", "Username is required"));
    bindingResult.addError(new FieldError("target", "email", "Email is invalid"));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/signup");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    // When
    ResponseEntity<ProblemDetail> response = exceptionHandler.handleValidation(
      ex, webRequest, Locale.ENGLISH, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("VALIDATION_ERROR");
    assertThat(response.getBody().getDetail()).contains("username: Username is required");
    assertThat(response.getBody().getDetail()).contains("email: Email is invalid");
  }

  @Test
  void handleNotFound_ShouldReturnNotFound() {
    // Given
    ResourceNotFoundException ex = new ResourceNotFoundException("UserEntity not found");
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/123");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    // When
    ResponseEntity<ProblemDetail> response = exceptionHandler.handleNotFound(
      ex, webRequest, request, Locale.ENGLISH);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("NOT_FOUND");
    assertThat(response.getBody().getDetail()).isEqualTo("UserEntity not found");
  }
}
