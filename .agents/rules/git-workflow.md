---
description: Git workflow and commit conventions for the Banking API
---

# Git Workflow Rules

## Branch Naming
| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feature/<short-description>` | `feature/account-management` |
| Bugfix | `bugfix/<short-description>` | `bugfix/transfer-validation` |
| Hotfix | `hotfix/<short-description>` | `hotfix/jwt-expiry` |
| Release | `release/<version>` | `release/1.0.0` |

## Commit Messages — Conventional Commits
Format: `<type>(<scope>): <description>`

### Types
| Type | Usage |
|------|-------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `refactor` | Code refactoring (no feature/fix) |
| `test` | Adding or updating tests |
| `chore` | Build, CI, tooling changes |
| `perf` | Performance improvement |
| `style` | Formatting, semicolons, etc. |

### Scopes
Use module name: `account`, `transaction`, `auth`, `config`, `security`, `docker`

### Examples
```
feat(account): add account creation endpoint
fix(transaction): validate insufficient balance before transfer
test(account): add unit tests for AccountService
docs(readme): update API documentation
chore(docker): add Redis container to docker-compose
refactor(security): extract JWT logic into dedicated service
```

## Branching Strategy
1. `main` — production-ready code only
2. `develop` — integration branch for features
3. Feature branches from `develop`
4. Merge via Pull Request with code review
5. Squash merge preferred for clean history

## Rules
- Never commit directly to `main` or `develop`
- Every PR must have a description
- Delete branch after merge
- Tag releases with semantic versioning: `v1.0.0`
