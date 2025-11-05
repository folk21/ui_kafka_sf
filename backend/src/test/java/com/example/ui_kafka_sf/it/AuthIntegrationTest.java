package com.example.ui_kafka_sf.it;

import com.example.ui_kafka_sf.auth.dto.LoginReq;
import com.example.ui_kafka_sf.auth.dto.RegisterReq;
import com.example.ui_kafka_sf.auth.Role;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

  // --- PostgreSQL Testcontainer ---
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("postgres")
      .withPassword("postgres");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
    r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    r.add("spring.jpa.show-sql", () -> "true");
    r.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    // если в конфиге что-то слушает Kafka — отключим автоконфиг
    r.add("spring.kafka.bootstrap-servers", () -> "disabled:9092");
    // Полностью выключаем Kafka-автоконфигурацию
    r.add("spring.autoconfigure.exclude", () -> String.join(",",
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "org.springframework.boot.autoconfigure.kafka.KafkaReactiveAutoConfiguration"
    ));
    r.add("app.jwt.secret", () -> "0123456789_0123456789_0123456789_01"); // >=32 байт
    r.add("app.kafka.enabled", () -> "false");        // отключить все kafka-бины
    r.add("app.kafka.topic",   () -> "sf.events");    // на всякий случай
  }

  // --- Глушим Kafka, чтобы не требовать живого брокера на тестах ---
  @MockBean
  KafkaTemplate<String, Object> kafkaTemplate;

  @LocalServerPort
  int port;

  @Autowired
  TestRestTemplate rest;

  private String url(String p) { return "http://localhost:" + port + p; }

  @BeforeEach
  void restTemplateWithOkHttp() {
    OkHttpClient client = new OkHttpClient.Builder()
        .retryOnConnectionFailure(false)
        .build();

    var factory = new OkHttp3ClientHttpRequestFactory(client);
    rest.getRestTemplate().setRequestFactory(factory);
    rest.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
      @Override protected boolean hasError(HttpStatusCode status) { return false; }
    });
  }

  @Test
  void register_and_login_instructor_ok() {
    // 1) register
    var reg = new RegisterReq("alice", "alice_pwd", Role.INSTRUCTOR);
    var regResp = rest.postForEntity(url("/api/auth/register"), reg, Map.class);
    if (!regResp.getStatusCode().is2xxSuccessful()) {
      System.err.println("REGISTER FAILED: status=" + regResp.getStatusCode()
          + " body=" + regResp.getBody());
    }
    assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(regResp.getBody()).containsEntry("status", "ok");

    // 2) login
    var login = new LoginReq("alice", "alice_pwd");
    var loginResp = rest.postForEntity(url("/api/auth/login"), login, Map.class);
    assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    var body = loginResp.getBody();
    assertThat(body).isNotNull();
    assertThat(body.get("token")).isInstanceOf(String.class);
    assertThat(((String) body.get("token")).length()).isGreaterThan(10);
    assertThat(body.get("username")).isEqualTo("alice");
    assertThat(body.get("role")).isEqualTo("INSTRUCTOR");
  }

  @Test
  void register_conflict_user_exists() {
    // создаём пользователя
    var reg1 = new RegisterReq("bob", "pwd", Role.STUDENT);
    var r1 = rest.postForEntity(url("/api/auth/register"), reg1, Map.class);
    assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.OK);

    // повторная регистрация того же пользователя должна вернуть 400
    var reg2 = new RegisterReq("bob", "pwd2", Role.STUDENT);
    var r2 = rest.postForEntity(url("/api/auth/register"), reg2, Map.class);
    assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(r2.getBody()).containsEntry("error", "user_exists");
  }

  @Test
  void login_invalid_password_unauthorized() {
    var reg = new RegisterReq("carol", "carol_pwd", Role.ADMIN);
    rest.postForEntity(url("/api/auth/register"), reg, Map.class);

    var badLogin = new LoginReq("carol", "wrong");
    var resp = rest.postForEntity(url("/api/auth/login"), badLogin, Map.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(resp.getBody()).containsEntry("error", "invalid_credentials");
  }
}
