# ui_kafka_sf

Spring Boot demo with **Auth + Kafka + DynamoDB** and a simple UI.

## Modules
- `backend` — Spring Boot app: auth (`/api/auth/register`, `/api/auth/login`), Kafka producer/consumer, DynamoDB writes.
- `ui` — Front-end (React/Vite or similar) for basic flows.
- `localstack` — init-scripts and docs for LocalStack/DynamoDB (инфраструктура поднимается **из корневого** `docker-compose.yml`).
- `sf-service` — stub external processor service.

## Quick start (dev)
```bash
# 1) Start dev infra (from project root)
docker compose up -d

# 2) Run backend
./gradlew :backend:bootRun

# 3) Run UI
cd ui && npm install && npm run dev
```

## Tests
```bash
./gradlew :backend:test
```

## Clean & rebuild
```bash
./gradlew clean build
```

## Database: PostgreSQL

The app now stores **Users** and **Courses** in PostgreSQL (JPA/Hibernate).
Run infra:
```bash
docker compose up -d
```
Backend:
```bash
./gradlew :backend:bootRun
```
UI:
```bash
cd ui && npm i && npm run dev
```
