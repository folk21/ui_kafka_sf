package com.example.ui_kafka_sf.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** UserRepository models application users and related authentication/authorization types. */
public interface UserRepository extends JpaRepository<User, String> {
  default Optional<User> findByUsername(String username) {
    return findById(username);
  }
}
