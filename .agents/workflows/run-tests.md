---
description: Workflow to run tests for the Banking API
---

# Run Tests Workflow

// turbo-all

## Unit Tests Only
```bash
cd c:\Code\JavaWeb\AI-java && mvnw test
```

## Integration Tests (requires Docker)
```bash
cd c:\Code\JavaWeb\AI-java && mvnw verify
```

## Run Specific Test Class
```bash
cd c:\Code\JavaWeb\AI-java && mvnw test -Dtest=AccountServiceTest
```

## Run Tests with Coverage Report
```bash
cd c:\Code\JavaWeb\AI-java && mvnw test jacoco:report
```

## Check Test Results
After running tests, check:
- Console output for pass/fail
- `target/surefire-reports/` for detailed reports
- `target/site/jacoco/index.html` for coverage (if jacoco configured)
