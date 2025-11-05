package com.example.ui_kafka_sf.sf;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** KafkaConfig supports idempotency persistence and/or Kafka integration for SF submissions. */
@Configuration
@ConditionalOnProperty(
    prefix = "app.kafka",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class KafkaConfig {

  @Bean
  @ConditionalOnMissingBean
  NewTopic sfTopic() {
    return TopicBuilder.name("sf.events")
        .partitions(1)
        .replicas(1)
        .config("cleanup.policy", "compact")
        .config("min.cleanable.dirty.ratio", "0.01")
        .build();
  }
}
