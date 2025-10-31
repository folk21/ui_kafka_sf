
package com.example.ui_kafka_sf.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
  private final AppProperties props;
  public JwtService(AppProperties props) { this.props = props; }

  public String issue(String username) {
    var key = Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    var now = Instant.now();
    var exp = now.plusSeconds(props.getJwt().getTtlMinutes() * 60L);
    return Jwts.builder()
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }
}
