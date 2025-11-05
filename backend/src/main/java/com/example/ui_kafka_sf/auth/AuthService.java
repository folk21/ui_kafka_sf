package com.example.ui_kafka_sf.auth;

import static java.util.Map.of;

import com.example.ui_kafka_sf.auth.dto.LoginReq;
import com.example.ui_kafka_sf.auth.dto.RegisterReq;
import com.example.ui_kafka_sf.auth.dto.TokenResp;
import com.example.ui_kafka_sf.auth.dto.UserRegisteredEvent;
import com.example.ui_kafka_sf.auth.util.JwtUtil;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Application service for authentication flows.
 *
 * <p>Contains the business logic previously embedded in {@link AuthController} methods: register,
 * login, and the diagnostic "me" view.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtUtil jwt;
  private final KafkaTemplate<String, Object> kafka;
  private final AppProperties props;

  /**
   * Registers a new user account.
   *
   * <p>Returns 200 OK on success; 400 with {@code error=user_exists} on duplicate.
   */
  public ResponseEntity<?> register(@Valid RegisterReq req) {
    var existing = users.findByUsername(req.username());
    if (existing.isPresent())
      return ResponseEntity.badRequest().body(of("error", "user_exists"));

    // create new User
    var u = new User();
    u.setUsername(req.username());
    u.setPasswordHash(encoder.encode(req.password()));
    u.setRole(req.role());

    // save User to DB
    if (!saveUserToDB(u))
      return ResponseEntity.badRequest().body(of("error", "user_exists"));

    // publish new User to Kafka topic
    publishUserRegisteredEvent(u);

    return ResponseEntity.ok(of("status", "ok"));
  }

  /**
   * Authenticates a user and issues a JWT on success.
   *
   * <p>Returns 200 OK with {@link TokenResp}; 401 with {@code error=invalid_credentials} otherwise.
   */
  public ResponseEntity<?> login(@Valid LoginReq req) {
    var u = users.findByUsername(req.username());
    if (u.isEmpty() || !encoder.matches(req.password(), u.get().getPasswordHash()))
      return ResponseEntity.status(401).body(of("error", "invalid_credentials"));
    var pair = jwt.issue(u.get().getUsername(), u.get().getRole().name());
    return ResponseEntity.ok(
        new TokenResp(
            pair.token(), pair.expiresInSec(), u.get().getUsername(), u.get().getRole().name()));
  }

  /** Returns a minimal view of the current security context. */
  public Map<String, Object> me(Authentication auth) {
    var roles =
        auth == null
            ? List.of()
            : auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
    return of("principal", auth == null ? null : auth.getName(), "roles", roles);
  }

  // -- HELPER METHODS

  /** Persists the user; returns false on duplicate (unique constraint race). */
  private boolean saveUserToDB(User u) {
    try {
      users.save(u);
      return true;
    } catch (DataIntegrityViolationException dup) {
      return false;
    }
  }

  /**
   * Publishes a UserRegisteredEvent to the configured Kafka topic so downstream consumers
   * can process the registration asynchronously (best-effort; does not affect HTTP flow).
   */
  private void publishUserRegisteredEvent(User u) {
    if (kafka == null || props.getKafka() == null) return;
    var topic = props.getKafka().getTopic();
    if (topic == null || topic.isBlank()) return;
    try {
      kafka.send(
          topic, new UserRegisteredEvent(u.getUsername(), u.getRole(), System.currentTimeMillis()));
    } catch (Exception ignore) {
      // keep API stable even if broker is down
    }
  }
}
