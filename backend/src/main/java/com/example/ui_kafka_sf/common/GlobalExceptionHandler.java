
package com.example.ui_kafka_sf.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(KafkaSfException.class)
  public ResponseEntity<?> handleKafkaSf(KafkaSfException ex){
    var e = ex.getError() == null ? Errors.UNKNOWN : ex.getError();
    return ResponseEntity.status(e.status).body(Map.of("error", e.code, "message", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex){
    return ResponseEntity.badRequest().body(Map.of("error","validation_failed"));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleIllegal(IllegalArgumentException ex){
    return ResponseEntity.badRequest().body(Map.of("error","bad_request", "message", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleOther(Exception ex){
    return ResponseEntity.internalServerError().body(Map.of("error","unknown_error"));
  }
}
