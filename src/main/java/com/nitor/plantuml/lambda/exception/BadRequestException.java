package com.nitor.plantuml.lambda.exception;

public class BadRequestException extends StatusCodeException {

  private final static int STATUS_CODE = 400;

  public BadRequestException() {
    super();
  }

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public int getStatusCode() {
    return STATUS_CODE;
  }
}
