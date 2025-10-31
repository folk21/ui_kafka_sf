
package com.example.ui_kafka_sf.it;

import com.example.ui_kafka_sf.auth.dto.RegisterReq;
import com.example.ui_kafka_sf.auth.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
public class AuthIntegrationTest {
  @Container
  static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));
  @Container
  static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.8"))
      .withServices(LocalStackContainer.Service.DYNAMODB);

  static String usersTopic = "users.registered";

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r){
    r.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    r.add("app.aws.region", () -> localstack.getRegion());
    r.add("app.aws.dynamodb.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
    r.add("app.kafka.users-topic", () -> usersTopic); // kebab-case
    r.add("app.kafka.usersTopic", () -> usersTopic); // camelCase fallback
  }

  @LocalServerPort int port;
  @Autowired TestRestTemplate http;

  static void ensureDdbTable(){
    var ddb = DynamoDbClient.builder()
        .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
        .region(software.amazon.awssdk.regions.Region.of(localstack.getRegion()))
        .build();
    try {
      ddb.createTable(CreateTableRequest.builder()
          .tableName("user")
          .attributeDefinitions(AttributeDefinition.builder().attributeName("username").attributeType(ScalarAttributeType.S).build())
          .keySchema(KeySchemaElement.builder().attributeName("username").keyType(KeyType.HASH).build())
          .billingMode(BillingMode.PAY_PER_REQUEST)
          .build());
    } catch (ResourceInUseException ignore) {}
  }

  private String url(String p) { return "http://localhost:"+port+p; }

  @Test @Order(1)
  void t1_register_then_duplicate_fails() {
    ensureDdbTable();
    var req = new RegisterReq("student@example.com", "pwd12345", Role.STUDENT);
    var resp1 = http.postForEntity(url("/api/auth/register"), req, Map.class);
    Assertions.assertEquals(200, resp1.getStatusCode().value(), "first register must succeed");

    var resp2 = http.postForEntity(url("/api/auth/register"), req, Map.class);
    Assertions.assertTrue(List.of(400, 409).contains(resp2.getStatusCode().value()), "duplicate must be 400/409");
  }

  @Test @Order(2)
  void t2_register_instructor_and_student_then_verify_kafka_and_ddb() throws Exception {
    ensureDdbTable();
    // Register instructor
    var instr = new RegisterReq("instructor@example.com", "pwd", Role.INSTRUCTOR);
    var r1 = http.postForEntity(url("/api/auth/register"), instr, Map.class);
    Assertions.assertEquals(200, r1.getStatusCode().value());

    // Register student
    var stud = new RegisterReq("bob@example.com", "pwd", Role.STUDENT);
    var r2 = http.postForEntity(url("/api/auth/register"), stud, Map.class);
    Assertions.assertEquals(200, r2.getStatusCode().value());

    // Verify Kafka has events
    Properties props = new Properties();
    props.put("bootstrap.servers", kafka.getBootstrapServers());
    props.put("group.id", "test-consumer");
    props.put("auto.offset.reset", "earliest");
    props.put("key.deserializer", StringDeserializer.class.getName());
    props.put("value.deserializer", JsonDeserializer.class.getName());
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(List.of(usersTopic));
      ConsumerRecords<String, Object> recs = consumer.poll(Duration.ofSeconds(10));
      boolean hasStudent = false;
      boolean hasInstructor = false;
      for (ConsumerRecord<String, Object> rec : recs) {
        if (rec.value() != null && rec.value().toString().contains("username")) {
          String v = rec.value().toString();
          hasStudent |= v.contains("bob@example.com");
          hasInstructor |= v.contains("instructor@example.com");
        }
      }
      Assertions.assertTrue(hasStudent, "Kafka must contain student registration event");
      Assertions.assertTrue(hasInstructor, "Kafka must contain instructor registration event");
    }

    // Verify DynamoDB contains student item (written by consumer)
    var ddb = DynamoDbClient.builder()
        .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
        .region(software.amazon.awssdk.regions.Region.of(localstack.getRegion()))
        .build();
    var get = ddb.getItem(GetItemRequest.builder()
        .tableName("user")
        .key(Map.of("username", AttributeValue.builder().s("bob@example.com").build()))
        .build());
    Assertions.assertTrue(get.hasItem(), "DynamoDB must contain student item");
  }
}
