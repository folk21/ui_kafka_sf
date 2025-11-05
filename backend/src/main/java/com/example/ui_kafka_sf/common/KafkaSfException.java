package com.example.ui_kafka_sf.common;

/**
 * Runtime exception for domain-level errors with a typed {@link Errors} code.
 *
 * <p>Use to signal business/integration failures (e.g., duplicate, Kafka publish issue),
 * so upper layers (ControllerAdvice) can map {@link #getError()} to a stable HTTP response.
 */
public class KafkaSfException extends RuntimeException {
  private final Errors error;

  /**
   * Constructs an exception with a semantic error code and message.
   *
   * @param error   domain error category (drives HTTP/status mapping)
   * @param message human-readable context for logs/diagnostics
   */
  public KafkaSfException(Errors error, String message) {
    super(message);
    this.error = error;
  }

  /**
   * Returns the domain error code associated with this failure.
   *
   * <p>Typical use: translate to HTTP status/body in an exception handler.
   */
  public Errors getError() {
    return error;
  }
}
