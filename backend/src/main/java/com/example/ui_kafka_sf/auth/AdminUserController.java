package com.example.ui_kafka_sf.auth;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

  private final UserRepository users;
  private final PasswordEncoder encoder;

  public AdminUserController(UserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  public record UserView(String username, Role role) {
    public static UserView from(User u) { return new UserView(u.getUsername(), u.getRole()); }
  }
  public record ChangePasswordReq(@NotBlank String newPassword) {}

  /** Список всех пользователей (логины + роли) */
  @GetMapping
  public List<UserView> all() {
    return users.findAll().stream().map(UserView::from).toList();
  }

  /** Смена пароля конкретному пользователю (админская) */
  @PutMapping("/{username}/password")
  public ResponseEntity<?> changePassword(@PathVariable String username, @RequestBody ChangePasswordReq req) {
    var u = users.findByUsername(username).orElse(null);
    if (u == null) {
      return ResponseEntity.notFound().build();
    }
    u.setPasswordHash(encoder.encode(req.newPassword()));
    users.save(u);
    return ResponseEntity.ok().build();
  }
}
