package com.example.ui_kafka_sf.common;

/** KafkaSfException is part of the application's domain layer. */
public class KafkaSfException extends RuntimeException {
  private final Errors error;

  public KafkaSfException(Errors error, String message) {
    super(message);
    this.error = error;
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  public Errors getError() {
    return error;
  }
}
