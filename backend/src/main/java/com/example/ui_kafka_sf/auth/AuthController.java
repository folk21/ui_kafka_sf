package com.example.ui_kafka_sf.auth;

import static java.util.Map.*;

import com.example.ui_kafka_sf.auth.dto.LoginReq;
import com.example.ui_kafka_sf.auth.dto.RegisterReq;
import com.example.ui_kafka_sf.auth.dto.TokenResp;
import com.example.ui_kafka_sf.auth.dto.UserRegisteredEvent;
import com.example.ui_kafka_sf.auth.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtUtil jwt;
  private final KafkaTemplate<String, Object> kafka;
  private final AppProperties props;

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

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterReq req) {
    var existing = users.findByUsername(req.username());
    if (existing.isPresent()) {
      return ResponseEntity.badRequest().body(of("error","user_exists"));
    }

    var u = new User();
    u.setUsername(req.username());
    u.setPasswordHash(encoder.encode(req.password()));
    u.setRole(req.role());
    try {
      users.save(u);
    } catch (DataIntegrityViolationException dup) {
      // параллельная гонка/дубль — вернуть предсказуемо 400
      return ResponseEntity.badRequest().body(of("error", "user_exists"));
    }
    // безопасная публикация (в тестах KafkaTemplate замокан)
    var topic = (props.getKafka()!=null) ? props.getKafka().getTopic() : null;
    if (kafka != null && topic != null && !topic.isBlank()) {
      try {
        kafka.send(topic, new UserRegisteredEvent(
            req.username(), u.getRole(), System.currentTimeMillis()
        ));
      } catch (Exception ignore) { /* no-op */ }
    }
    return ResponseEntity.ok(of("status","ok"));
  }

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
}
