---
description: Workflow to start the development environment with Docker
---

# Docker Dev Workflow

// turbo-all

## Start All Services
```bash
cd c:\Code\JavaWeb\AI-java && docker-compose up -d
```

## Check Service Status
```bash
cd c:\Code\JavaWeb\AI-java && docker-compose ps
```

## Start the Spring Boot Application
```bash
cd c:\Code\JavaWeb\AI-java && mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Verify Services
- **PostgreSQL**: `localhost:5432` (user: `banking_user`, pass: `banking_secret`, db: `banking_db`)
- **Redis**: `localhost:6379`
- **RabbitMQ Management UI**: `http://localhost:15672` (user: `banking_user`, pass: `banking_secret`)
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`

## View Logs
```bash
cd c:\Code\JavaWeb\AI-java && docker-compose logs -f postgres
cd c:\Code\JavaWeb\AI-java && docker-compose logs -f redis
cd c:\Code\JavaWeb\AI-java && docker-compose logs -f rabbitmq
```

## Stop All Services
```bash
cd c:\Code\JavaWeb\AI-java && docker-compose down
```

## Reset (Delete Data Volumes)
```bash
cd c:\Code\JavaWeb\AI-java && docker-compose down -v
```
