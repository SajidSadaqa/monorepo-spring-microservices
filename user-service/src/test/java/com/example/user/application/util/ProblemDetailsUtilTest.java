package com.example.user.application.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemDetailsUtilTest {

  @Test
  void pd_ShouldCreateProblemDetailWithAllFields() {
    // Given
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    // When
    ProblemDetail result = ProblemDetailsUtil.pd(400, "TEST_ERROR", "Test detail", webRequest);

    // Then
    assertThat(result.getStatus()).isEqualTo(400);
    assertThat(result.getTitle()).isEqualTo("TEST_ERROR");
    assertThat(result.getDetail()).isEqualTo("Test detail");
    assertThat(result.getProperties()).containsKey("timestamp");
    assertThat(result.getProperties()).containsKey("path");
    assertThat(result.getProperties()).containsKey("code");
    assertThat(result.getProperties().get("code")).isEqualTo("TEST_ERROR");
  }
}
