package com.example.user.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class GlobalExceptionHandlerTest {

  @Test
  void mapsBusinessToProblemDetail() {
    StaticMessageSource ms = new StaticMessageSource();
    ms.addMessage("error.internal", Locale.ENGLISH, "Internal");
    GlobalExceptionHandler geh = new GlobalExceptionHandler(ms);
    var pd = geh.handleBusiness(new BusinessException("X","custom"), new ServletWebRequest(new MockHttpServletRequest()));
    assertThat(pd.getTitle()).isEqualTo("BUSINESS_ERROR");
  }
}
