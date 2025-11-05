package com.example.ui_kafka_sf.sf;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SfIdempotencyRepository extends JpaRepository<SfIdempotencyEntity, String> {

  /**
   * Пытается «зарезервировать» первый приём сообщения.
   * Возвращает true, если записи ещё не было и мы её создали;
   * false — если такая запись уже существует.
   *
   * Реализация идемпотентна и потокобезопасна за счёт уникальности PK
   * и перехвата DataIntegrityViolationException при гонке.
   */
  @Transactional
  default boolean tryReserveFirstSend(String email, String keyHash) {
    try {
      var e = SfIdempotencyEntity.builder()
          .keyHash(keyHash)
          .email(email)
          .build();
      save(e); // INSERT; при дубле — DataIntegrityViolationException
      return true;
    } catch (DataIntegrityViolationException duplicate) {
      return false;
    }
  }
}
