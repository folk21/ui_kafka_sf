
package com.example.ui_kafka_sf.common;

import org.springframework.http.HttpStatus;

public enum Errors {
  DUPLICATE_USER(HttpStatus.CONFLICT, "duplicate_user"),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "invalid_credentials"),
  FORBIDDEN_SELF_REGISTER_ADMIN(HttpStatus.BAD_REQUEST, "forbidden_self_register_admin"),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "validation_failed"),
  UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, "unknown_error");

  public final HttpStatus status;
  public final String code;
  Errors(HttpStatus status, String code) {
    this.status = status;
    this.code = code;
  }
}
