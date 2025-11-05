package com.example.ui_kafka_sf.sf;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * SfIdempotencyRepository supports idempotency persistence and/or Kafka integration for SF
 * submissions.
 */
public interface SfIdempotencyRepository extends JpaRepository<SfIdempotencyEntity, String> {

  @Transactional
  default boolean tryReserveFirstSend(String email, String keyHash) {
    try {
      var e = SfIdempotencyEntity.builder().keyHash(keyHash).email(email).build();
      save(e);
      return true;
    } catch (DataIntegrityViolationException duplicate) {
      return false;
    }
  }
}
