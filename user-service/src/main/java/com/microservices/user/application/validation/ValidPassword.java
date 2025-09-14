package com.microservices.user.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface ValidPassword {
  String message() default "{auth.password.invalid}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  int min() default 8;
  int max() default 72;
  boolean requireUppercase() default true;
  boolean requireLowercase() default true;
  boolean requireDigit() default true;
  boolean requireSpecialChar() default false;
  String allowedSpecialChars() default "!@#$%^&*()_+-=[]{}|;:,.<>?";
}
