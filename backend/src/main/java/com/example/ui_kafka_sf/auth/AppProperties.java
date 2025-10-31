package com.example.ui_kafka_sf.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application configuration bound from properties with prefix {@code app}.
 * Uses Lombok {@code @Data} to avoid manual getters/setters.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private Jwt jwt = new Jwt();
  private Kafka kafka = new Kafka();
  private Aws aws = new Aws();

  @Data
  public static class Jwt {
    private String secret;
    private int ttlMinutes = 120;
  }

  @Data
  public static class Kafka {
    /** Topic for SF events */
    private String topic = "sf.events";
    /** Topic for user registration events */
    private String usersTopic = "users.registered";
  }

  @Data
  public static class Aws {
    private String region = "us-east-1";
    private Dynamo dynamodb = new Dynamo();
  }

  @Data
  public static class Dynamo {
    private String endpoint;
    private String table = "user";
    private String sfTable = "sf_contact";
  }
}
