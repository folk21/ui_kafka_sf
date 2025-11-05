package com.example.ui_kafka_sf.auth;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * AdminUserController exposes authentication endpoints (register, login, JWT issuance) and acts as
 * an entry point for auth flows.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

  private final UserRepository users;
  private final PasswordEncoder encoder;

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  public record UserView(String username, Role role) {
    /** Performs a unit of domain logic; see README for the surrounding flow. */
    public static UserView from(User u) {
      return new UserView(u.getUsername(), u.getRole());
    }
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  public record ChangePasswordReq(@NotBlank String newPassword) {}

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @GetMapping
  public List<UserView> all() {
    return users.findAll().stream().map(UserView::from).toList();
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @PutMapping("/{username}/password")
  public ResponseEntity<?> changePassword(
      @PathVariable String username, @RequestBody ChangePasswordReq req) {
    var u = users.findByUsername(username).orElse(null);
    if (u == null) {
      return ResponseEntity.notFound().build();
    }
    u.setPasswordHash(encoder.encode(req.newPassword()));
    users.save(u);
    return ResponseEntity.ok().build();
  }
}
