package com.example.ui_kafka_sf.auth;

import com.example.ui_kafka_sf.auth.dto.UserRegisteredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * UserRegistrationConsumer models application users and related authentication/authorization types.
 */
@Component
public class UserRegistrationConsumer {

  private final UserRepository users;

  public UserRegistrationConsumer(UserRepository users) {
    this.users = users;
  }

  /** Handles an incoming domain or messaging event. */
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
