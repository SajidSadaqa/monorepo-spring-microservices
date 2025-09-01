package com.example.user.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import com.example.user.interfaces.exception.BusinessException;
import com.example.user.interfaces.exception.RestExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

class RestExceptionHandlerTest {

  @Test
  void mapsBusinessToProblemDetail() {
    // Message source (if the handler resolves localized messages elsewhere)
    StaticMessageSource ms = new StaticMessageSource();
    ms.addMessage("error.internal", Locale.ENGLISH, "Internal");

    // Environment (use dev to surface extra props if needed)
    MockEnvironment env = new MockEnvironment().withProperty("spring.profiles.active", "dev");

    // System under test
    RestExceptionHandler geh = new RestExceptionHandler(ms, env);

    // Request scaffolding
    MockHttpServletRequest http = new MockHttpServletRequest("GET", "/users");
    WebRequest req = new ServletWebRequest(http);

    // Given a domain/business exception
    BusinessException ex = new BusinessException("X", "custom");

    // When
    ResponseEntity<ProblemDetail> response = geh.handleBusiness(ex, req, http);
    ProblemDetail pd = response.getBody();

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(pd).isNotNull();
    assertThat(pd.getTitle()).isEqualTo("BUSINESS_ERROR");
    assertThat(pd.getDetail()).isEqualTo("custom");
    assertThat(pd.getProperties()).containsKey("path"); // added by enrich()
  }
}
