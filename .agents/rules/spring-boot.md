---
description: Spring Boot conventions and best practices for the Banking API
---

# Spring Boot Rules

## Dependency Injection
- **Always use constructor injection** via `@RequiredArgsConstructor`
- Never use `@Autowired` on fields
- Declare dependencies as `private final`

## Architecture (Layered)
```
Controller → Service (interface) → ServiceImpl → Repository → Entity
```
- **Controller**: Only handles HTTP, validation, and delegates to Service
- **Service**: All business logic lives here
- **Repository**: Data access only — no business logic
- **Config**: All configuration in dedicated `@Configuration` classes

## Database
- Use **Spring Data JPA** repositories
- Database migrations via **Flyway** (production) or `ddl-auto: update` (dev only)
- Always use `@Transactional` on service methods that modify data
- Use `@Transactional(readOnly = true)` for read-only operations

## Configuration
- Use `application.yml` format (not `.properties`)
- Externalize secrets via environment variables: `${ENV_VAR:defaultValue}`
- Use Spring Profiles: `dev`, `staging`, `prod`
- Custom properties under `app.*` namespace

## API
- All endpoints prefixed with `/api/v1/`
- Use `@RestController` + `@RequestMapping`
- Every endpoint must have **Swagger annotations** (`@Operation`, `@Tag`)
- Return `ResponseEntity<ApiResponse<T>>`

## Error Handling
- Global handler via `@RestControllerAdvice`
- Custom exceptions extend `RuntimeException`
- Consistent error response format: `{ success, message, data, errors, timestamp }`

## Security
- JWT-based authentication
- Role-based access: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_MANAGER`
- Protect endpoints with `@PreAuthorize`
- Password encoding via `BCryptPasswordEncoder`
