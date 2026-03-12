---
description: REST API design standards for the Banking API
---

# API Design Rules

## URL Convention
- Use **nouns**, not verbs: `/accounts` not `/getAccounts`
- Plural resource names: `/accounts`, `/transactions`
- Nested resources: `/accounts/{id}/transactions`
- API versioning via URL: `/api/v1/...`

## HTTP Methods
| Method | Usage | Example |
|--------|-------|---------|
| GET | Retrieve resource(s) | `GET /api/v1/accounts/{id}` |
| POST | Create resource | `POST /api/v1/accounts` |
| PUT | Full update | `PUT /api/v1/accounts/{id}` |
| PATCH | Partial update | `PATCH /api/v1/accounts/{id}` |
| DELETE | Remove resource | `DELETE /api/v1/accounts/{id}` |

## HTTP Status Codes
| Code | Usage |
|------|-------|
| 200 | Successful GET/PUT/PATCH |
| 201 | Successful POST (created) |
| 204 | Successful DELETE |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict (e.g., duplicate) |
| 422 | Unprocessable Entity |
| 500 | Internal Server Error |

## Request/Response
- Always validate requests with `@Valid`
- Use dedicated Request/Response DTOs per endpoint
- Wrap responses in standard envelope:
```json
{
  "success": true,
  "message": "Account created successfully",
  "data": { ... },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Pagination
- Use query params: `?page=0&size=20&sort=createdAt,desc`
- Response includes: `content`, `totalElements`, `totalPages`, `currentPage`

## Filtering & Search
- Filter via query params: `?status=ACTIVE&type=SAVINGS`
- Search via: `?search=keyword`
- Date range: `?fromDate=2024-01-01&toDate=2024-12-31`

## Documentation
- Every controller class: `@Tag(name = "...", description = "...")`
- Every endpoint: `@Operation(summary = "...", description = "...")`
- Every DTO field: `@Schema(description = "...", example = "...")`
