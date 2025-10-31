package com.example.ui_kafka_sf.sf;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
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
