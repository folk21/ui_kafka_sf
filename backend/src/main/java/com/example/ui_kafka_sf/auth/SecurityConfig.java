package com.example.ui_kafka_sf.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * SecurityConfig configures HTTP security, JWT filter chain, public endpoints, and role-based
 * authorization rules.
 */
@Configuration
public class SecurityConfig {

  private final AppProperties props;

  public SecurityConfig(AppProperties props) {
    this.props = props;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.cors(Customizer.withDefaults());
    http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.anonymous(Customizer.withDefaults());

    http.authorizeHttpRequests(
        auth ->
            auth.requestMatchers(
                    "/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/actuator/**")
                .permitAll()
                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()
                .anyRequest()
                .authenticated());

    http.addFilterBefore(new JwtFilter(props), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    var cors = new CorsConfiguration();
    cors.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
    cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    cors.setAllowedHeaders(List.of("*"));
    cors.setAllowCredentials(false);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    return source;
  }

  static class JwtFilter extends OncePerRequestFilter {
    private final AppProperties props;

    JwtFilter(AppProperties props) {
      this.props = props;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

      String uri = request.getRequestURI();
      if ("OPTIONS".equalsIgnoreCase(request.getMethod())
          || uri.startsWith("/api/auth")
          || uri.startsWith("/swagger")
          || uri.startsWith("/v3/api-docs")
          || uri.startsWith("/actuator")) {
        filterChain.doFilter(request, response);
        return;
      }

      String hdr = request.getHeader("Authorization");
      if (hdr != null && hdr.startsWith("Bearer ")) {
        var token = hdr.substring(7);
        try {
          var key = Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
          var parsed = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
          Claims claims = parsed.getPayload();
          String username = claims.getSubject();
          String roleFromToken = null;
          Object roleClaim = claims.get("role");
          if (roleClaim instanceof String s && StringUtils.hasText(s)) {
            roleFromToken = s.trim();
          } else {
            Object rolesClaim = claims.get("roles");
            if (rolesClaim instanceof List<?> list
                && !list.isEmpty()
                && list.get(0) instanceof String s0) {
              roleFromToken = s0.trim();
            }
          }
          if (StringUtils.hasText(username) && StringUtils.hasText(roleFromToken)) {

            String authority =
                roleFromToken.startsWith("ROLE_") ? roleFromToken : "ROLE_" + roleFromToken;

            var auth =
                new UsernamePasswordAuthenticationToken(
                    username, null, List.of(new SimpleGrantedAuthority(authority)));
            SecurityContextHolder.getContext().setAuthentication(auth);
          }
        } catch (Exception ignored) {
        }
      }
      filterChain.doFilter(request, response);
    }
  }
}
