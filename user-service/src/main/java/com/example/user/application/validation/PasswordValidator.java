package com.example.user.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

  private int min;
  private int max;
  private boolean requireUppercase;
  private boolean requireLowercase;
  private boolean requireDigit;
  private boolean requireSpecialChar;
  private String allowedSpecialChars;

  @Override
  public void initialize(ValidPassword constraintAnnotation) {
    this.min = constraintAnnotation.min();
    this.max = constraintAnnotation.max();
    this.requireUppercase = constraintAnnotation.requireUppercase();
    this.requireLowercase = constraintAnnotation.requireLowercase();
    this.requireDigit = constraintAnnotation.requireDigit();
    this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    this.allowedSpecialChars = constraintAnnotation.allowedSpecialChars();
  }

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null) {
      return false;
    }

    // Check length
    if (password.length() < min || password.length() > max) {
      addViolation(context, "Password must be between " + min + " and " + max + " characters");
      return false;
    }

    // Check uppercase requirement
    if (requireUppercase && !Pattern.compile("[A-Z]").matcher(password).find()) {
      addViolation(context, "Password must contain at least one uppercase letter");
      return false;
    }

    // Check lowercase requirement
    if (requireLowercase && !Pattern.compile("[a-z]").matcher(password).find()) {
      addViolation(context, "Password must contain at least one lowercase letter");
      return false;
    }

    // Check digit requirement
    if (requireDigit && !Pattern.compile("\\d").matcher(password).find()) {
      addViolation(context, "Password must contain at least one digit");
      return false;
    }

    // Check special character requirement
    if (requireSpecialChar) {
      String escapedSpecialChars = Pattern.quote(allowedSpecialChars);
      if (!Pattern.compile("[" + escapedSpecialChars + "]").matcher(password).find()) {
        addViolation(context, "Password must contain at least one special character");
        return false;
      }
    }

    // Check for invalid characters (only if we want to restrict to specific character sets)
    String allowedPattern = buildAllowedPattern();
    if (!Pattern.compile("^[" + allowedPattern + "]+$").matcher(password).matches()) {
      addViolation(context, "Password contains invalid characters");
      return false;
    }

    return true;
  }

  private String buildAllowedPattern() {
    StringBuilder pattern = new StringBuilder();
    pattern.append("A-Za-z0-9"); // Always allow alphanumeric

    if (requireSpecialChar || !allowedSpecialChars.isEmpty()) {
      // Escape special regex characters for use in character class
      String escapedChars = allowedSpecialChars
        .replace("\\", "\\\\")
        .replace("]", "\\]")
        .replace("^", "\\^")
        .replace("-", "\\-");
      pattern.append(escapedChars);
    }

    return pattern.toString();
  }

  private void addViolation(ConstraintValidatorContext context, String message) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
