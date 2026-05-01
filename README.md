# Notification Core
A high-performance notification engine built with Java 21, designed to handle massive concurrency using Virtual Threads and ensuring "at-least-once" delivery with robust resilience patterns.

## 🚀 Key Features
* Java 21 Virtual Threads: Massive throughput with minimal memory footprint by leveraging Project Loom.
* Redis Idempotency: Prevents duplicate notifications using a fail-open SETNX strategy.
* Resilience4j Retries: Exponential backoff for flaky downstream SMS/Email providers.
* Dead Letter Queue (DLQ): Automatic persistence of failed notifications to H2/JPA for manual recovery.
* Strategy Pattern: Easily extensible architecture for adding new channels (Email, SMS, Push, etc.).

## 🛠️ Tech Stack
Framework: Spring Boot 3.4+

* Runtime: Java 21 (Temurin)
* Database: H2 (DLQ Storage)
* Cache: Redis (Idempotency)
* Documentation: SpringDoc OpenAPI (Swagger)

## 🚦 Quick Start
Prerequisites

* JDK 21
* Maven 3.9+
* Redis (Local or Cloud)

## API Usage

```POST /v1/notifications
Header: Idempotency-Key: <unique-uuid>

JSON
{
  "userId": "user_123",
  "channel": "EMAIL",
  "templateId": "welcome_email",
  "payload": {
    "name": "John Doe"
  }
}
```

## 📦 CI/CD
Automated pipeline via GitHub Actions:
* Build: Compiles on JDK 21.
* Test: Runs integration tests with Redis/DB mocks.
* Deploy: Pushes Docker image to GitHub Container Registry (GHCR).

## License
MIT
