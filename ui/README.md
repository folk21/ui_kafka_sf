
# UI

Simple front-end for auth flows.

## Run
```bash
npm install
npm run dev
```

## Gotchas
- Ensure backend is running on `http://localhost:8080` (or adjust `.env`).
- If Kafka/LocalStack are down, registration may work locally but events/consumer won't.
