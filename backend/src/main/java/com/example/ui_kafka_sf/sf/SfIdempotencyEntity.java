package com.example.ui_kafka_sf.sf;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "sf_idempotency")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "keyHash")
@ToString
public class SfIdempotencyEntity {

  /** Уникальный хеш запроса (email|fullName|message) */
  @Id
  @Column(name = "key_hash", nullable = false, updatable = false, length = 128)
  private String keyHash;

  @Column(nullable = false, length = 320)
  private String email;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
