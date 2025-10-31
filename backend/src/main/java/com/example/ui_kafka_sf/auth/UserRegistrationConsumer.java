package com.example.ui_kafka_sf.auth;

import com.example.ui_kafka_sf.auth.dto.UserRegisteredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.Map;

/**
 * Consumes UserRegisteredEvent and writes user to DynamoDB.
 * Idempotent via conditional put: attribute_not_exists(username).
 */
@Component
public class UserRegistrationConsumer {

  private final DynamoDbClient ddb;
  private final String table;

  public UserRegistrationConsumer(AppProperties props) {
    var creds = AwsBasicCredentials.create(
        System.getenv().getOrDefault("AWS_ACCESS_KEY_ID", "test"),
        System.getenv().getOrDefault("AWS_SECRET_ACCESS_KEY", "test")
    );
    var b = DynamoDbClient.builder()
        .region(Region.of(props.getAws().getRegion()))
        .credentialsProvider(StaticCredentialsProvider.create(creds));
    if (props.getAws().getDynamodb().getEndpoint() != null) {
      b = b.endpointOverride(URI.create(props.getAws().getDynamodb().getEndpoint()));
    }
    this.ddb = b.build();
    this.table = props.getAws().getDynamodb().getTable();
    ensureTable();
  }

  private void ensureTable() {
    try {
      ddb.describeTable(DescribeTableRequest.builder().tableName(table).build());
    } catch (ResourceNotFoundException rnfe) {
      ddb.createTable(CreateTableRequest.builder()
          .tableName(table)
          .keySchema(KeySchemaElement.builder().attributeName("username").keyType(KeyType.HASH).build())
          .attributeDefinitions(AttributeDefinition.builder().attributeName("username").attributeType(ScalarAttributeType.S).build())
          .billingMode(BillingMode.PAY_PER_REQUEST)
          .build());
      ddb.waiter().waitUntilTableExists(DescribeTableRequest.builder().tableName(table).build());
    }
  }

  @KafkaListener(topics = "#{@appProperties.kafka.usersTopic}", groupId = "ui-kafka-sf-users")
  public void onUserRegistered(UserRegisteredEvent evt) {
    try {
      ddb.putItem(PutItemRequest.builder()
          .tableName(table)
          .item(Map.of(
              "username", AttributeValue.builder().s(evt.username()).build(),
              "passwordHash", AttributeValue.builder().s("<external>").build(), // у нас нет пароля в событии
              "role", AttributeValue.builder().s(evt.role().name()).build()
          ))
          .conditionExpression("attribute_not_exists(username)")
          .build());
    } catch (ConditionalCheckFailedException ignore) {
      // already exists — no-op
    }
  }
}
