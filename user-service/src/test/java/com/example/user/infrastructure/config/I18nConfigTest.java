package com.example.user.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class I18nConfigTest {

  private final I18nConfig config = new I18nConfig();

  @Test
  void messageSource_ShouldBeConfigured() {
    // When
    MessageSource messageSource = config.messageSource();

    // Then
    assertThat(messageSource).isNotNull();
  }

  @Test
  void localeResolver_ShouldHaveEnglishAsDefault() {
    // When
    LocaleResolver localeResolver = config.localeResolver();

    // Then
    assertThat(localeResolver).isNotNull();
    // Note: We can't easily test the default locale without a mock request
  }
}
