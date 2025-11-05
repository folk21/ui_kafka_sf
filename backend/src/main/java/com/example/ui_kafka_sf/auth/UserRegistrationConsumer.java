package com.example.ui_kafka_sf.auth;

import com.example.ui_kafka_sf.auth.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Kafka consumer that reacts to {@link UserRegisteredEvent} messages. */
@Component
@RequiredArgsConstructor
public class UserRegistrationConsumer {

  private final UserRepository users;

  /**
   * Handles {@link UserRegisteredEvent} and performs an idempotent user upsert.
   *
   * <p>Steps:
   *
   * <ol>
   *   <li>Validate payload (ignore when event or username is null).
   *   <li>{@code findByUsername}:
   *       <ul>
   *         <li>If present → no-op (user already exists).
   *         <li>If absent → create a minimal {@link User} with role from the event and a
   *             placeholder password.
   *       </ul>
   * </ol>
   *
   * <p>Side effects: writes to the users table on first-seen usernames.
   */
  @KafkaListener(topics = "${app.kafka.topic}")
  public void onUserRegistered(UserRegisteredEvent evt) {
    if (evt == null || evt.username() == null) return;
    users
        .findByUsername(evt.username())
        .ifPresentOrElse(
            u -> {
              /* already exists - no-op */
            },
            () -> {
              var u = new User();
              u.setUsername(evt.username());
              u.setPasswordHash("<external>");
              u.setRole(evt.role());
              users.save(u);
            });
  }
}
