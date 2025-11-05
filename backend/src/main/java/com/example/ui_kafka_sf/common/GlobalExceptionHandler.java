package com.example.ui_kafka_sf.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** GlobalExceptionHandler is part of the application's domain layer. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @ExceptionHandler(KafkaSfException.class)
  public ResponseEntity<?> handleKafkaSf(KafkaSfException ex) {
    var e = ex.getError() == null ? Errors.UNKNOWN : ex.getError();
    return ResponseEntity.status(e.status)
        .body(Map.of("error", e.code, "message", ex.getMessage()));
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest().body(Map.of("error", "validation_failed"));
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleIllegal(IllegalArgumentException ex) {
    return ResponseEntity.badRequest()
        .body(Map.of("error", "bad_request", "message", ex.getMessage()));
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleOther(Exception ex) {
    return ResponseEntity.internalServerError().body(Map.of("error", "unknown_error"));
  }
}
