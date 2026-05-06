Idempotent Payment Processing API

This project is a Spring Boot service that demonstrates **idempotent payment processing** using an `Idempotency-Key` header.
It guarantees safe retries for clients by returning the same response for duplicate requests with the same key and request body.

## Architecture Diagram

```mermaid
flowchart TD
    A[Client] -->|POST /process-payment<br/>Idempotency-Key| B[PaymentController]
    B --> C[IdempotencyService]
    C --> D[In-Memory Store<br/>ConcurrentHashMap]
    C --> E[HashUtil<br/>Base64(JSON body)]
    F[TTLCleanupTask<br/>@Scheduled every 60s] --> D
```

### Request Handling Flow

1. Client sends `POST /process-payment` with `Idempotency-Key` and JSON body.
2. `PaymentController` delegates to `IdempotencyService`.
3. Service computes a request hash from the body (`HashUtil`).
4. If key exists:
   - Same body -> cached result is returned (`X-Cache-Hit: true`).
   - Different body -> conflict response (`409`).
   - In-flight original request -> duplicate waits for completion and gets same result.
5. If key does not exist:
   - Service creates a record and processes payment simulation.
   - Response is stored and returned (`201 Created`).
6. `TTLCleanupTask` removes expired idempotency records after a 5-minute TTL.

## Setup Instructions

### Prerequisites

- Java 17+
- Maven 3.9+ (or use the included Maven Wrapper)

### Run Locally

Using Maven Wrapper (recommended):

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs on:

- `http://localhost:8080`

### Build and Test

```bash
./mvnw clean test
./mvnw clean package
```

## API Documentation

### Base URL

- `http://localhost:8080`

### Endpoint

- `POST /process-payment`

### Headers

- `Idempotency-Key` (required): unique key per logical payment request.

### Request Body (example)

```json
{
  "amount": 100,
  "currency": "USD"
}
```

### Responses

- `201 Created`: first successful processing of a new idempotency key.
- `201 Created` + `X-Cache-Hit: true`: repeated request with same key/body.
- `409 Conflict`: same key reused with a different body.

### cURL Examples

First request:

```bash
curl -X POST http://localhost:8080/process-payment \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: pay-123" \
  -d '{"amount":100,"currency":"USD"}'
```

Retry same request (returns cached response):

```bash
curl -X POST http://localhost:8080/process-payment \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: pay-123" \
  -d '{"amount":100,"currency":"USD"}'
```

Same key, different body (conflict):

```bash
curl -X POST http://localhost:8080/process-payment \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: pay-123" \
  -d '{"amount":200,"currency":"USD"}'
```

## Design Decisions

- **In-memory store (`ConcurrentHashMap`)**: chosen for simplicity and clarity of idempotency logic in a demo project.
- **Request fingerprinting via body hash**: ensures the same key cannot silently process different payloads.
- **In-flight request coalescing**: `CompletableFuture` lets concurrent duplicates wait for the first request result.
- **TTL-based cleanup**: prevents unbounded memory growth by expiring keys after 5 minutes.
- **Explicit cache-hit response header**: `X-Cache-Hit: true` makes replayed responses visible to clients.

## The Developer's Choice: Extra Feature

### Added Feature: Automatic Expiration and Cleanup of Idempotency Records

An extra feature implemented in this project is **automatic TTL cleanup** using a scheduled task:

- Runs every 60 seconds.
- Deletes records older than 5 minutes.
- Keeps memory usage under control without manual intervention.

This makes the idempotency mechanism production-oriented compared to a basic demo that never expires keys.



