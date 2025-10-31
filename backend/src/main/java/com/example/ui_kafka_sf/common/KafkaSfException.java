
package com.example.ui_kafka_sf.common;

public class KafkaSfException extends RuntimeException {
  private final Errors error;

  public KafkaSfException(Errors error, String message) {
    super(message);
    this.error = error;
  }

  public Errors getError() { return error; }
}
