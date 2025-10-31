package com.example.ui_kafka_sf.auth;

import com.example.ui_kafka_sf.auth.dto.LoginReq;
import com.example.ui_kafka_sf.auth.dto.RegisterReq;
import com.example.ui_kafka_sf.auth.dto.TokenResp;
import com.example.ui_kafka_sf.auth.dto.UserRegisteredEvent;
import com.example.ui_kafka_sf.auth.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import static java.util.Map.*;

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
    if (req.role() == Role.ADMIN) {
      return ResponseEntity.badRequest()
          .body(of("error", "self-registration as ADMIN is not allowed"));
    }
    if (users.findByUsername(req.username()).isPresent()) {
      return ResponseEntity.badRequest().body(of("error", "username already exists"));
    }
// 1) Синхронно сохраним для мгновенного логина
    var entity = new UserEntity(req.username(), encoder.encode(req.password()), req.role());
    users.save(entity);

    // 2) Опубликуем доменное событие (идемпотентную запись сделает consumer)
    var evt = new UserRegisteredEvent(req.username(), req.role(), System.currentTimeMillis());
    kafka.send(props.getKafka().getUsersTopic(), req.username(), evt);

    return ResponseEntity.ok(of("status", "registered"));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginReq req) {
    var u = users.findByUsername(req.username());
    if (u.isEmpty() || !encoder.matches(req.password(), u.get().passwordHash())) {
      return ResponseEntity.status(401).body(of("error", "invalid_credentials"));
    }
    var pair = jwt.issue(u.get().username(), u.get().role().name());
    return ResponseEntity.ok(
        new TokenResp(
            pair.token(), pair.expiresInSec(), u.get().username(), u.get().role().name()));
  }
}
