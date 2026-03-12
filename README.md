# 🏦 Banking API

A production-ready **Banking & Finance REST API** built with **Java 21** and **Spring Boot 3.4**.

## 🚀 Features

- **Account Management** — Create, update, and manage bank accounts
- **Fund Transfers** — Secure money transfers with transaction validation
- **Transaction History** — Paginated, filterable transaction records
- **Authentication & Authorization** — JWT + Role-based access (User, Manager, Admin)
- **Audit Logging** — Track all operations with timestamps and user info
- **API Documentation** — Interactive Swagger UI

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.4 (Java 21) |
| Security | Spring Security + JWT |
| Database | PostgreSQL 16 + Flyway |
| ORM | Spring Data JPA / Hibernate |
| Caching | Redis 7 |
| Message Queue | RabbitMQ 3 |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Containerization | Docker + Docker Compose |

## 📋 Prerequisites

- **Java 21** (or higher)
- **Maven 3.9+** (or use included Maven wrapper `./mvnw`)
- **Docker & Docker Compose** (for database and services)

## ⚡ Quick Start

### 1. Clone the repository
```bash
git clone https://github.com/your-username/banking-api.git
cd banking-api
```

### 2. Start infrastructure services
```bash
docker-compose up -d
```

### 3. Run the application
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Open Swagger UI
```
http://localhost:8080/api/swagger-ui.html
```

## 📁 Project Structure

```
src/main/java/com/banking/api/
├── config/          # Security, CORS, Swagger, Audit, Redis
├── controller/      # REST Controllers
├── service/         # Business Logic (interface + impl)
├── repository/      # Spring Data JPA Repositories
├── model/
│   ├── entity/      # JPA Entities (extend BaseEntity)
│   ├── dto/         # Request/Response DTOs  
│   ├── enums/       # Enumerations
│   └── mapper/      # MapStruct Mappers
├── security/        # JWT Provider, Filter, UserDetails
├── exception/       # Custom Exceptions + Global Handler
└── util/            # Constants, Helpers
```

## 🧪 Running Tests

```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker)
./mvnw verify

# With coverage report
./mvnw test jacoco:report
```

## 🐳 Docker Services

| Service | Port | Credentials |
|---------|------|-------------|
| PostgreSQL | 5432 | `banking_user` / `banking_secret` |
| Redis | 6379 | — |
| RabbitMQ | 5672 / 15672 (UI) | `banking_user` / `banking_secret` |

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login |
| GET | `/api/v1/accounts` | List accounts |
| POST | `/api/v1/accounts` | Create account |
| POST | `/api/v1/transactions/transfer` | Fund transfer |
| GET | `/api/v1/transactions` | Transaction history |

## 📝 License

This project is licensed under the MIT License.
