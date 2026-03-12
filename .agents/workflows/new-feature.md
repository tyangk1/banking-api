---
description: Workflow to implement a new feature end-to-end
---

# New Feature Workflow

Follow these steps when implementing a new feature:

// turbo-all

## Steps

1. Create a feature branch:
```bash
git checkout -b feature/<feature-name>
```

2. Design the database schema changes and create Flyway migration if needed:
   - Create migration file: `src/main/resources/db/migration/V<N>__<description>.sql`

3. Create the Entity (follow `.agents/skills/spring-entity/SKILL.md`):
   - Place in `com.banking.api.model.entity`

4. Create Enums if needed:
   - Place in `com.banking.api.model.enums`

5. Create Repository:
   - Place in `com.banking.api.repository`

6. Create DTOs (Request + Response):
   - Place in `com.banking.api.model.dto.request` and `com.banking.api.model.dto.response`

7. Create MapStruct Mapper:
   - Place in `com.banking.api.model.mapper`

8. Create Service interface + implementation:
   - Interface in `com.banking.api.service`
   - Implementation in `com.banking.api.service.impl`

9. Create Controller with Swagger annotations:
   - Place in `com.banking.api.controller`

10. Write unit tests for Service:
```bash
mvnw test -Dtest=<ServiceName>Test
```

11. Write controller tests:
```bash
mvnw test -Dtest=<ControllerName>Test
```

12. Run all tests to verify nothing is broken:
```bash
mvnw test
```

13. Commit with conventional commit message:
```bash
git add .
git commit -m "feat(<scope>): <description>"
```
