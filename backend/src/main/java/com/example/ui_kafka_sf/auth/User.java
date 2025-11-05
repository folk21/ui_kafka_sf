package com.example.ui_kafka_sf.auth;

import jakarta.persistence.*;
import lombok.*;

/** User models application users and related authentication/authorization types. */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "username")
@ToString(exclude = "passwordHash")
public class User {

  @Id private String username;

  @Column(nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;
}
