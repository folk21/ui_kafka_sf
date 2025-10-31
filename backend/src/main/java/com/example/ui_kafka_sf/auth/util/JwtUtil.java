package com.example.ui_kafka_sf.auth.util;

import com.example.ui_kafka_sf.auth.AppProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
  private final AppProperties props;
  public JwtUtil(AppProperties props){ this.props = props; }

  public record Pair(String token, long expiresInSec) {}

  public Pair issue(String username, String role) {
    var key = Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    long ttlSec = props.getJwt().getTtlMinutes() * 60L;
    var now = Instant.now();
    var exp = now.plusSeconds(ttlSec);
    var token = Jwts.builder()
        .subject(username)
        .claim("role", role)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
    return new Pair(token, ttlSec);
  }
}
