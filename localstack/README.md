
# Local dev stack (LocalStack + Redpanda)

This module provides a simple `docker-compose.yml` to run:
- **LocalStack** (DynamoDB) on `http://localhost:4566`
- **Redpanda** (Kafka-compatible) on `localhost:9092`

## Run
В корне проекта запустить:
```bash
docker compose up -d
```

DynamoDB tables are auto-created via `./init/01-ddb.sh` (user, sf_contact).
