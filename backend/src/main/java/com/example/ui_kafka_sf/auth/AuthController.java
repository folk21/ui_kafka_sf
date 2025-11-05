package com.example.ui_kafka_sf.auth;

import static java.util.Map.*;

import com.example.ui_kafka_sf.auth.dto.LoginReq;
import com.example.ui_kafka_sf.auth.dto.RegisterReq;
import com.example.ui_kafka_sf.auth.dto.TokenResp;
import com.example.ui_kafka_sf.auth.dto.UserRegisteredEvent;
import com.example.ui_kafka_sf.auth.util.JwtUtil;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Register new users (persist user, hash password, assign role).
 *   <li>Authenticate users (verify password) and issue JWT tokens.
 *   <li>Expose a lightweight endpoint to introspect the current principal and authorities.
 * </ul>
 *
 * <p>Security & contracts:
 *
 * <ul>
 *   <li>Endpoints are under {@code /api/auth/**}. Typically permitted for anonymous use in
 *       SecurityConfig.
 *   <li>On successful registration returns {@code 200 OK}; on duplicate user returns {@code 400 Bad
 *       Request}.
 *   <li>On login with invalid credentials returns {@code 401 Unauthorized}; on success returns a
 *       JWT and metadata.
 * </ul>
 *
 * <p>Side effects:
 *
 * <ul>
 *   <li>Optionally publishes {@link UserRegisteredEvent} to Kafka when a user is registered (if
 *       Kafka is configured).
 * </ul>
 *
 * <p>Concurrency/consistency:
 *
 * <ul>
 *   <li>The controller is stateless and thread-safe; duplicate-user races are handled by a
 *       defensive catch of {@link DataIntegrityViolationException} to surface a stable {@code 400}.
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtUtil jwt;
  private final KafkaTemplate<String, Object> kafka;
  private final AppProperties props;

  /**
   * Constructs the controller with all required dependencies.
   *
   * @param users repository to read/write {@link User} records
   * @param encoder password hashing/verification service
   * @param jwt utility that issues signed JWTs and returns token metadata
   * @param kafka Kafka template for publishing domain events (may be disabled in some environments)
   * @param props application properties; used to resolve Kafka topic and JWT configuration
   */
  public AuthController(
      UserRepository users,
      PasswordEncoder encoder,
      JwtUtil jwt,
      KafkaTemplate<String, Object> kafka,
      AppProperties props) {
    this.users = users;
    this.encoder = encoder;
    this.jwt = jwt;
    this.kafka = kafka;
    this.props = props;
  }

  /**
   * Registers a new user account.
   *
   * <p>Flow:
   *
   * <ol>
   *   <li>Reject if a user with the same username already exists (fast path lookup).
   *   <li>Persist a new {@link User} with a hashed password and the requested role.
   *   <li>Guard against a parallel duplicate insert by catching {@link
   *       DataIntegrityViolationException} and returning a stable {@code 400} with {@code
   *       error=user_exists}.
   *   <li>If Kafka is configured and a topic is present, publish {@link UserRegisteredEvent}
   *       (errors are swallowed to avoid impacting the API contract).
   * </ol>
   *
   * @param req validated registration payload (username, raw password, role)
   * @return {@code 200 OK} on success; {@code 400 Bad Request} with {@code error=user_exists} if
   *     duplicate
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterReq req) {
    var existing = users.findByUsername(req.username());
    if (existing.isPresent()) {
      return ResponseEntity.badRequest().body(of("error", "user_exists"));
    }

    var u = new User();
    u.setUsername(req.username());
    u.setPasswordHash(encoder.encode(req.password()));
    u.setRole(req.role());
    try {
      users.save(u);
    } catch (DataIntegrityViolationException dup) {
      // parallel race / unique constraint violation → respond with a predictable 400
      return ResponseEntity.badRequest().body(of("error", "user_exists"));
    }

    // Best-effort event publication (non-blocking for API stability)
    var topic = (props.getKafka() != null) ? props.getKafka().getTopic() : null;
    if (kafka != null && topic != null && !topic.isBlank()) {
      try {
        kafka.send(
            topic,
            new UserRegisteredEvent(req.username(), u.getRole(), System.currentTimeMillis()));
      } catch (Exception ignore) {
        /* no-op: do not fail the registration */
      }
    }
    return ResponseEntity.ok(of("status", "ok"));
  }

  /**
   * Authenticates a user and issues a JWT on success.
   *
   * <p>Flow:
   *
   * <ol>
   *   <li>Load the user by username; if not found or password mismatch, return {@code 401} with
   *       {@code error=invalid_credentials}.
   *   <li>Issue a signed JWT via {@link JwtUtil#issue(String, String)} where {@code sub=username}
   *       and a role claim is included.
   *   <li>Return a {@link TokenResp} containing the token and auxiliary metadata (expiry seconds,
   *       username, role).
   * </ol>
   *
   * @param req validated login payload (username, raw password)
   * @return {@code 200 OK} with token on success; {@code 401 Unauthorized} on invalid credentials
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginReq req) {
    var u = users.findByUsername(req.username());
    if (u.isEmpty() || !encoder.matches(req.password(), u.get().getPasswordHash())) {
      return ResponseEntity.status(401).body(of("error", "invalid_credentials"));
    }
    var pair = jwt.issue(u.get().getUsername(), u.get().getRole().name());
    return ResponseEntity.ok(
        new TokenResp(
            pair.token(), pair.expiresInSec(), u.get().getUsername(), u.get().getRole().name()));
  }

  /**
   * Returns a minimal view of the current security context.
   *
   * <p>Purpose:
   *
   * <ul>
   *   <li>Lightweight diagnostic endpoint to confirm the effective principal and granted
   *       authorities on the server.
   *   <li>If unauthenticated, returns {@code principal=null} and an empty authority list.
   * </ul>
   *
   * @param auth the Spring Security {@link Authentication} injected from the current context
   * @return a map with {@code principal} and {@code roles} keys for convenience
   */
  @GetMapping("/api/auth/me")
  public Map<String, Object> me(Authentication auth) {
    var roles =
        auth == null
            ? List.of()
            : auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
    return of("principal", auth == null ? null : auth.getName(), "roles", roles);
  }
}
