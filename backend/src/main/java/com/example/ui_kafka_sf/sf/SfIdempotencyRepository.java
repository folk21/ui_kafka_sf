package com.example.ui_kafka_sf.sf;

import com.example.ui_kafka_sf.auth.AppProperties;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

/** Repository for idempotency control of Salesforce submissions via DynamoDB. */
@Repository
public class SfIdempotencyRepository {
  private final DynamoDbClient ddb;
  private final String table;

  public SfIdempotencyRepository(AppProperties props) {
    var creds = AwsBasicCredentials.create(
        System.getenv().getOrDefault("AWS_ACCESS_KEY_ID", "test"),
        System.getenv().getOrDefault("AWS_SECRET_ACCESS_KEY", "test")
    );
    var builder = DynamoDbClient.builder()
        .region(Region.of(props.getAws().getRegion()))
        .credentialsProvider(StaticCredentialsProvider.create(creds));
    if (props.getAws().getDynamodb().getEndpoint() != null) {
      builder = builder.endpointOverride(URI.create(props.getAws().getDynamodb().getEndpoint()));
    }
    this.ddb = builder.build();
    this.table = props.getAws().getDynamodb().getSfTable();
    ensureTable();
  }

  /** Create table if missing; partition key: email (String). */
  private void ensureTable() {
    try {
      ddb.describeTable(DescribeTableRequest.builder().tableName(table).build());
    } catch (ResourceNotFoundException rnfe) {
      ddb.createTable(CreateTableRequest.builder()
          .tableName(table)
          .keySchema(KeySchemaElement.builder().attributeName("email").keyType(KeyType.HASH).build())
          .attributeDefinitions(AttributeDefinition.builder().attributeName("email").attributeType(ScalarAttributeType.S).build())
          .billingMode(BillingMode.PAY_PER_REQUEST)
          .build());
      ddb.waiter().waitUntilTableExists(DescribeTableRequest.builder().tableName(table).build());
    }
  }

  /**
   * Reserve first submission per email using conditional put.
   * @return true when inserted (first time); false when duplicate.
   */
  public boolean tryReserveFirstSend(String email, String payloadHash) {
    try {
      ddb.putItem(PutItemRequest.builder()
          .tableName(table)
          .item(Map.of(
              "email", AttributeValue.builder().s(email).build(),
              "createdAt", AttributeValue.builder().s(Instant.now().toString()).build(),
              "payloadHash", AttributeValue.builder().s(payloadHash).build()
          ))
          .conditionExpression("attribute_not_exists(email)")
          .build());
      return true;
    } catch (ConditionalCheckFailedException dup) {
      return false;
    }
  }
}
