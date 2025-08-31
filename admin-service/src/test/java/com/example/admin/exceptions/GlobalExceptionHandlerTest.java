package com.example.admin.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

@SpringBootTest
class GlobalExceptionHandlerTest {
  @Autowired MessageSource ms;
  @Test
  void businessExceptionToProblemDetail() {
    GlobalExceptionHandler geh = new GlobalExceptionHandler(ms);
    var req = new ServletWebRequest(new MockHttpServletRequest());
    var pd = geh.handleBusiness(new BusinessException("BUS", "message"), req);
    assertThat(pd.getTitle()).isEqualTo("BUSINESS_ERROR");
  }
}
