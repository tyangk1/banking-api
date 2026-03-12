---
name: spring-security
description: Guide to setting up JWT-based authentication for the Banking API
---

# Spring Security Skill

Use this skill when implementing or modifying authentication/authorization.

## Architecture

```
Request → JwtAuthenticationFilter → SecurityContext → Controller
                ↓
         JwtTokenProvider (validate/parse token)
                ↓
         CustomUserDetailsService (load user from DB)
```

## Components

### 1. JwtTokenProvider
Handles JWT creation, validation, and parsing:
- `generateAccessToken(UserDetails)` → JWT string
- `generateRefreshToken(UserDetails)` → JWT string
- `validateToken(String token)` → boolean
- `getUsernameFromToken(String token)` → String
- Uses `app.jwt.secret` and `app.jwt.expiration-ms` from config

### 2. JwtAuthenticationFilter
Extends `OncePerRequestFilter`:
- Extract token from `Authorization: Bearer <token>` header
- Validate token via `JwtTokenProvider`
- Load user via `CustomUserDetailsService`
- Set `SecurityContext` authentication

### 3. CustomUserDetailsService
Implements `UserDetailsService`:
- Load user from `UserRepository`
- Map roles to `GrantedAuthority`

### 4. SecurityConfig
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

## Auth Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | User registration |
| POST | `/api/v1/auth/login` | Login → returns access + refresh token |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | Invalidate token |

## Roles

| Role | Access |
|------|--------|
| `ROLE_USER` | Own account, transactions |
| `ROLE_MANAGER` | Approve transactions, view reports |
| `ROLE_ADMIN` | Full system access |

## Protecting Endpoints
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public ResponseEntity<?> getAllUsers() { ... }

@PreAuthorize("hasAnyRole('USER', 'MANAGER')")
@GetMapping("/accounts")
public ResponseEntity<?> getMyAccounts() { ... }

@PreAuthorize("#userId == authentication.principal.id")
@GetMapping("/users/{userId}/accounts")
public ResponseEntity<?> getUserAccounts(@PathVariable String userId) { ... }
```
