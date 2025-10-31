
# ui_kafka_sf

Spring Boot + Redpanda (Kafka) + React (dev UI) + DynamoDB users + JWT auth (2h) + Prometheus/Actuator.
Kinesis removed (replaced with Kafka/Redpanda). Diagnostics/Prometheus kept.

## Quick start (dev)
```bash
# 1) Start infra (Redpanda + Console + LocalStack: DynamoDB)
docker compose up -d

# 2) Backend
./gradlew :backend:bootRun

# 3) Frontend (dev)
cd ui && npm i && npm run dev

# Login default:
#   username: admin
#   password: admin123
# After login, token stored in localStorage. Expires 2 hours.
```

## Notes
- DynamoDB table: `user` (partition key: `username`).
- SF "send" is modeled as producing JSON to Kafka topic `sf.events`.
- AWS credentials for DynamoDB picked from env (works with LocalStack by default).


## Record migration
- Core data classes converted to Java `record` where applicable (UserEntity, SfEvent, DTOs).
- Lombok `@Data` kept for `AppProperties`.
