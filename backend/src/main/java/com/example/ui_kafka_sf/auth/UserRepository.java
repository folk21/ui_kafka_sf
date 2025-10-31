
package com.example.ui_kafka_sf.auth;

import com.example.ui_kafka_sf.auth.Role;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {
  private final DynamoDbClient ddb;
  private final String table;

  public UserRepository(AppProperties props) {
    var creds = AwsBasicCredentials.create(System.getenv().getOrDefault("AWS_ACCESS_KEY_ID", "test"),
        System.getenv().getOrDefault("AWS_SECRET_ACCESS_KEY", "test"));
    var builder = DynamoDbClient.builder()
        .region(Region.of(props.getAws().getRegion()))
        .credentialsProvider(StaticCredentialsProvider.create(creds));
    if (props.getAws().getDynamodb().getEndpoint() != null) {
      builder = builder.endpointOverride(URI.create(props.getAws().getDynamodb().getEndpoint()));
    }
    this.ddb = builder.build();
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

  public Optional<UserEntity> findByUsername(String username) {
    var resp = ddb.getItem(GetItemRequest.builder().tableName(table)
        .key(Map.of("username", AttributeValue.builder().s(username).build()))
        .consistentRead(true).build());
    if (resp.hasItem() && !resp.item().isEmpty()) {
      var item = resp.item();
      return Optional.of(new UserEntity(
        item.get("username").s(),
        item.get("passwordHash").s(),
        Role.valueOf(item.getOrDefault("role", AttributeValue.builder().s("STUDENT").build()).s())
      ));
    }
    return Optional.empty();
  }

  public void save(UserEntity user) {
    ddb.putItem(PutItemRequest.builder().tableName(table)
        .item(Map.of(
            "username", AttributeValue.builder().s(user.username()).build(),
            "passwordHash", AttributeValue.builder().s(user.passwordHash()).build()
        )).build());
  }
}
