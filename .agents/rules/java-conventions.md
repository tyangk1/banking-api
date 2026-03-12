---
description: Java coding conventions for the Banking API project
---

# Java Conventions

## Language Version
- Use **Java 21** features where appropriate: records, sealed classes, pattern matching, text blocks

## Style Guide
- Follow **Google Java Style Guide**
- Max line length: **120 characters**
- Use **4 spaces** for indentation (no tabs)

## Lombok
- Use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` for DTOs
- Use `@Getter`, `@Setter`, `@ToString` for entities (avoid `@Data` on entities)
- Use `@RequiredArgsConstructor` for constructor injection
- Use `@Slf4j` for logging

## Naming
- Classes: `PascalCase`
- Methods/Variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `lowercase`
- DTOs: suffix with `Request` or `Response` (e.g., `CreateAccountRequest`)
- Services: Interface + Impl pattern (e.g., `AccountService`, `AccountServiceImpl`)

## Patterns
- **DTO Pattern**: Never expose JPA entities directly in controllers
- **Builder Pattern**: Use Lombok `@Builder` for objects with many fields
- **Optional**: Use `Optional` return types from repositories, never pass as method parameter
- Wrap all API responses in `ApiResponse<T>`

## Code Quality
- No magic numbers — use constants
- No `System.out.println` — use SLF4J logger
- Handle `null` explicitly — use `Optional` or `@NonNull`
- Keep methods under **30 lines**
- Keep classes under **300 lines**
