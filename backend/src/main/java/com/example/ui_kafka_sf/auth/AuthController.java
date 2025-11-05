package com.example.ui_kafka_sf.auth;

import com.example.ui_kafka_sf.auth.dto.LoginReq;
import com.example.ui_kafka_sf.auth.dto.RegisterReq;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller.
 *
 * <p>Delegates all business logic to {@link AuthService}.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /** Registers a new user account (delegates to {@link AuthService#register}). */
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterReq req) {
    return authService.register(req);
  }

  /** Authenticates a user and issues a JWT (delegates to {@link AuthService#login}). */
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginReq req) {
    return authService.login(req);
  }

  /**
   * Returns a minimal view of the current security context (delegates to {@link AuthService#me}).
   */
  @GetMapping("/api/auth/me")
  public Map<String, Object> me(Authentication auth) {
    return authService.me(auth);
  }
}
