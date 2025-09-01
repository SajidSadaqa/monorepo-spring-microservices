package com.example.admin.interfaces.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
  private final String code;

  public BusinessException(String code, String message) {
    super(message);
    this.code = code;
  }
}
