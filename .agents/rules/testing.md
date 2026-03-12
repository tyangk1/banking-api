---
description: Testing standards and practices for the Banking API
---

# Testing Rules

## Test Structure
```
src/test/java/com/banking/api/
├── controller/    # @WebMvcTest — Controller layer tests
├── service/       # @ExtendWith(MockitoExtension) — Unit tests
├── repository/    # @DataJpaTest + Testcontainers — Repository tests
└── integration/   # @SpringBootTest + Testcontainers — Full integration
```

## Naming Convention
```
should_<ExpectedResult>_when_<Condition>()
```
Examples:
- `should_CreateAccount_when_ValidRequest()`
- `should_ThrowException_when_InsufficientBalance()`
- `should_ReturnPagedAccounts_when_ValidPagination()`

## Unit Tests (Service Layer)
- Use `@ExtendWith(MockitoExtension.class)`
- Mock all dependencies with `@Mock`
- Inject service under test with `@InjectMocks`
- Test happy path AND error cases
- Verify interactions with `verify()`

## Controller Tests
- Use `@WebMvcTest(XxxController.class)`
- Mock service layer with `@MockBean`
- Use `MockMvc` for HTTP testing
- Test: status codes, response body, validation errors

## Repository Tests
- Use `@DataJpaTest` with Testcontainers PostgreSQL
- Test custom queries
- Verify data persistence

## Integration Tests
- Use `@SpringBootTest` with Testcontainers
- Test full request/response cycle
- Include authentication in tests

## Coverage
- **Minimum 70%** line coverage
- Service layer: **90%+** coverage
- Controller layer: test all endpoints
- Focus on business logic, not getters/setters

## Libraries
| Library | Purpose |
|---------|---------|
| JUnit 5 | Test framework |
| Mockito | Mocking |
| AssertJ | Fluent assertions |
| MockMvc | Controller testing |
| Testcontainers | Real database in tests |
| Spring Security Test | Auth testing |

## Test Data
- Use `@BeforeEach` for test setup
- Create test data builders or factory methods
- Never depend on external services in unit tests
