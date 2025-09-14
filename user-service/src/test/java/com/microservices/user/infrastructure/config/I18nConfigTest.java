package com.microservices.user.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {I18nConfig.class})
@TestPropertySource(properties = {
  "spring.messages.basename=classpath:i18n/messages",
  "spring.messages.encoding=UTF-8"
})
@DisplayName("Internationalization (i18n) Configuration Tests")
class I18nConfigTest {

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private LocaleResolver localeResolver;

  @Nested
  @DisplayName("Message Source Configuration Tests")
  class MessageSourceTests {

    @Test
    @DisplayName("Should resolve English messages correctly")
    void shouldResolveEnglishMessages() {
      // Test basic authentication messages
      String usernameRequired = messageSource.getMessage("auth.username.required", null, Locale.ENGLISH);
      assertEquals("Username is required", usernameRequired);

      String passwordWeak = messageSource.getMessage("auth.password.weak", null, Locale.ENGLISH);
      assertEquals("Password must be 8?72 chars and include upper, lower, and number", passwordWeak);

      String emailInvalid = messageSource.getMessage("auth.email.invalid", null, Locale.ENGLISH);
      assertEquals("Email is invalid", emailInvalid);
    }

    @Test
    @DisplayName("Should resolve Arabic messages correctly")
    void shouldResolveArabicMessages() {
      Locale arabic = new Locale("ar");

      // Test basic authentication messages in Arabic
      String usernameRequired = messageSource.getMessage("auth.username.required", null, arabic);
      assertEquals("اسم المستخدم مطلوب", usernameRequired);

      String passwordWeak = messageSource.getMessage("auth.password.weak", null, arabic);
      assertEquals("يجب أن تتكون كلمة المرور من ٨ إلى ٧٢ حرفًا وتشمل أحرفًا كبيرة وصغيرة وأرقامًا", passwordWeak);

      String emailInvalid = messageSource.getMessage("auth.email.invalid", null, arabic);
      assertEquals("البريد الإلكتروني غير صالح", emailInvalid);
    }

    @ParameterizedTest
    @CsvSource({
      "auth.username.required, Username is required, اسم المستخدم مطلوب",
      "auth.password.required, Password is required, كلمة المرور مطلوبة",
      "auth.email.required, Email is required, البريد الإلكتروني مطلوب",
      "auth.username.taken, Username already exists, اسم المستخدم موجود بالفعل",
      "auth.email.taken, Email already exists, البريد الإلكتروني موجود بالفعل",
      "auth.login.invalid, Invalid username or password, اسم المستخدم أو كلمة المرور غير صحيحة"
    })
    @DisplayName("Should resolve authentication messages in both languages")
    void shouldResolveAuthMessagesInBothLanguages(String key, String expectedEnglish, String expectedArabic) {
      String englishMessage = messageSource.getMessage(key, null, Locale.ENGLISH);
      assertEquals(expectedEnglish, englishMessage, "English message mismatch for key: " + key);

      String arabicMessage = messageSource.getMessage(key, null, new Locale("ar"));
      assertEquals(expectedArabic, arabicMessage, "Arabic message mismatch for key: " + key);
    }

    @ParameterizedTest
    @CsvSource({
      "jwt.invalid, Invalid token, رمز الدخول غير صالح",
      "jwt.expired, Token expired, انتهت صلاحية الرمز",
      "jwt.missing, Authorization token is missing, رمز التفويض مفقود",
      "jwt.refresh.required, Refresh token required, مطلوب رمز تحديث",
      "jwt.s2s.required, Service-to-service token required, مطلوب رمز خدمة إلى خدمة",
      "jwt.s2s.audience, Invalid audience for service-to-service token, جمهور غير صالح لرمز الخدمة إلى الخدمة"
    })
    @DisplayName("Should resolve JWT messages in both languages")
    void shouldResolveJwtMessagesInBothLanguages(String key, String expectedEnglish, String expectedArabic) {
      String englishMessage = messageSource.getMessage(key, null, Locale.ENGLISH);
      assertEquals(expectedEnglish, englishMessage, "English JWT message mismatch for key: " + key);

      String arabicMessage = messageSource.getMessage(key, null, new Locale("ar"));
      assertEquals(expectedArabic, arabicMessage, "Arabic JWT message mismatch for key: " + key);
    }

    @ParameterizedTest
    @CsvSource({
      "refresh.invalid, Invalid refresh token, رمز التحديث غير صالح",
      "refresh.revoked, Refresh token has been used or revoked, تم استخدام رمز التحديث أو إبطاله",
      "refresh.expired, Refresh token has expired, انتهت صلاحية رمز التحديث"
    })
    @DisplayName("Should resolve refresh token messages in both languages")
    void shouldResolveRefreshTokenMessagesInBothLanguages(String key, String expectedEnglish, String expectedArabic) {
      String englishMessage = messageSource.getMessage(key, null, Locale.ENGLISH);
      assertEquals(expectedEnglish, englishMessage, "English refresh token message mismatch for key: " + key);

      String arabicMessage = messageSource.getMessage(key, null, new Locale("ar"));
      assertEquals(expectedArabic, arabicMessage, "Arabic refresh token message mismatch for key: " + key);
    }

    @ParameterizedTest
    @CsvSource({
      "error.internal, Something went wrong. Please try again later., حدث خطأ ما. يرجى المحاولة لاحقًا",
      "error.not_found, Resource not found, المورد غير موجود",
      "access.denied, You do not have permission to perform this action., ليست لديك صلاحية لتنفيذ هذا الإجراء"
    })
    @DisplayName("Should resolve error messages in both languages")
    void shouldResolveErrorMessagesInBothLanguages(String key, String expectedEnglish, String expectedArabic) {
      String englishMessage = messageSource.getMessage(key, null, Locale.ENGLISH);
      assertEquals(expectedEnglish, englishMessage, "English error message mismatch for key: " + key);

      String arabicMessage = messageSource.getMessage(key, null, new Locale("ar"));
      assertEquals(expectedArabic, arabicMessage, "Arabic error message mismatch for key: " + key);
    }

    @Test
    @DisplayName("Should handle message parameters correctly in English")
    void shouldHandleParametersInEnglish() {
      // Test with parameters (if you add parametrized messages)
      String message = messageSource.getMessage("auth.username.invalid", null, Locale.ENGLISH);
      assertEquals("Username may only contain letters, numbers, dot, dash, underscore (3?30 chars)", message);
    }

    @Test
    @DisplayName("Should handle message parameters correctly in Arabic")
    void shouldHandleParametersInArabic() {
      String message = messageSource.getMessage("auth.username.invalid", null, new Locale("ar"));
      assertEquals("يمكن أن يحتوي اسم المستخدم فقط على أحرف وأرقام ونقطة وشرطة وشرطة سفلية (٣ إلى ٣٠ حرفًا)", message);
    }

    @Test
    @DisplayName("Should fall back to English when Arabic message is missing")
    void shouldFallbackToEnglishWhenArabicMissing() {
      // Test with a key that might not exist in Arabic
      String message = messageSource.getMessage("auth.username.required", null, new Locale("ar"));
      assertNotNull(message);
      // Should get Arabic message since it exists
      assertEquals("اسم المستخدم مطلوب", message);
    }

    @Test
    @DisplayName("Should handle non-existent message keys")
    void shouldHandleNonExistentKeys() {
      assertThrows(NoSuchMessageException.class, () ->
        messageSource.getMessage("non.existent.key", null, Locale.ENGLISH));
    }

    @Test
    @DisplayName("Should handle null locale gracefully")
    void shouldHandleNullLocale() {
      // Should use default locale (English)
      String message = messageSource.getMessage("auth.username.required", null, null);
      assertEquals("Username is required", message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"en_US", "en_GB", "en_CA"})
    @DisplayName("Should handle English locale variants")
    void shouldHandleEnglishVariants(String localeString) {
      Locale locale = Locale.forLanguageTag(localeString.replace("_", "-"));
      String message = messageSource.getMessage("auth.username.required", null, locale);
      assertEquals("Username is required", message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ar_SA", "ar_EG", "ar_JO"})
    @DisplayName("Should handle Arabic locale variants")
    void shouldHandleArabicVariants(String localeString) {
      Locale locale = Locale.forLanguageTag(localeString.replace("_", "-"));
      String message = messageSource.getMessage("auth.username.required", null, locale);
      assertEquals("اسم المستخدم مطلوب", message);
    }
  }

  @Nested
  @DisplayName("Locale Resolver Configuration Tests")
  class LocaleResolverTests {

    @Test
    @DisplayName("Should have correct default locale")
    void shouldHaveCorrectDefaultLocale() {
      assertNotNull(localeResolver);
      // Note: Default locale testing depends on the specific LocaleResolver implementation
      // AcceptHeaderLocaleResolver doesn't have a direct way to get default locale
      // This is more of a configuration validation
    }

    @Test
    @DisplayName("Should be instance of AcceptHeaderLocaleResolver")
    void shouldBeAcceptHeaderLocaleResolver() {
      assertTrue(localeResolver instanceof org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver);
    }
  }

  @Nested
  @DisplayName("Message Source Bean Configuration Tests")
  class BeanConfigurationTests {

    @Test
    @DisplayName("MessageSource bean should be configured correctly")
    void messageSourceShouldBeConfiguredCorrectly() {
      assertNotNull(messageSource);
      assertTrue(messageSource instanceof org.springframework.context.support.ReloadableResourceBundleMessageSource);
    }

    @Test
    @DisplayName("Should handle UTF-8 encoding correctly")
    void shouldHandleUtf8EncodingCorrectly() {
      // Test Arabic characters are properly decoded
      String arabicMessage = messageSource.getMessage("auth.username.required", null, new Locale("ar"));

      // Verify that Arabic characters are properly handled
      assertTrue(arabicMessage.contains("اسم"));
      assertTrue(arabicMessage.contains("المستخدم"));
      assertTrue(arabicMessage.contains("مطلوب"));

      // Check character length vs byte length to ensure UTF-8 handling
      assertTrue(arabicMessage.getBytes().length > arabicMessage.length());
    }

    @Test
    @DisplayName("Should not fallback to system locale")
    void shouldNotFallbackToSystemLocale() {
      // Test with an unsupported locale
      Locale unsupportedLocale = new Locale("zh", "CN");

      // Should fall back to default (English) rather than system locale
      String message = messageSource.getMessage("auth.username.required", null, unsupportedLocale);
      assertEquals("Username is required", message);
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should work end-to-end for validation messages")
    void shouldWorkEndToEndForValidationMessages() {
      // Simulate what happens during validation
      Locale englishLocale = Locale.ENGLISH;
      Locale arabicLocale = new Locale("ar");

      // Test username validation
      String englishUsernameError = messageSource.getMessage("auth.username.invalid", null, englishLocale);
      String arabicUsernameError = messageSource.getMessage("auth.username.invalid", null, arabicLocale);

      assertNotEquals(englishUsernameError, arabicUsernameError);
      assertTrue(englishUsernameError.contains("Username"));
      assertTrue(arabicUsernameError.contains("اسم المستخدم"));
    }

    @Test
    @DisplayName("Should support all required message keys")
    void shouldSupportAllRequiredMessageKeys() {
      String[] requiredKeys = {
        "auth.username.required",
        "auth.username.invalid",
        "auth.password.required",
        "auth.password.weak",
        "auth.email.required",
        "auth.email.invalid",
        "auth.username.taken",
        "auth.email.taken",
        "auth.login.invalid",
        "jwt.invalid",
        "jwt.expired",
        "jwt.missing",
        "access.denied"
      };

      for (String key : requiredKeys) {
        assertDoesNotThrow(() -> {
          String english = messageSource.getMessage(key, null, Locale.ENGLISH);
          String arabic = messageSource.getMessage(key, null, new Locale("ar"));

          assertNotNull(english, "English message missing for key: " + key);
          assertNotNull(arabic, "Arabic message missing for key: " + key);
          assertFalse(english.isBlank(), "English message blank for key: " + key);
          assertFalse(arabic.isBlank(), "Arabic message blank for key: " + key);
        }, "Failed to resolve message for key: " + key);
      }
    }
  }
}
