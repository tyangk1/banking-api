---
name: spring-entity
description: Guide to creating a JPA Entity following Banking API conventions
---

# Spring Entity Skill

Use this skill whenever creating a new JPA Entity for the Banking API.

## Base Entity

All entities MUST extend `BaseEntity` which provides common fields:

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    @Version
    private Long version;
}
```

## Entity Checklist

When creating a new entity, follow these steps:

1. **Create entity class** in `com.banking.api.model.entity`
   - Extend `BaseEntity`
   - Add `@Entity` and `@Table(name = "table_name")`
   - Use `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
   - Add `@ToString(exclude = {"relationships"})` to avoid lazy load issues

2. **Define columns**
   - Use `@Column(nullable = false)` for required fields
   - Use `@Column(unique = true)` for unique fields
   - Use `@Enumerated(EnumType.STRING)` for enums
   - Add `@Column(precision = 19, scale = 4)` for monetary BigDecimal fields

3. **Define relationships**
   - Use `@ManyToOne(fetch = FetchType.LAZY)` — always LAZY
   - Use `@OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)`
   - Add `@JoinColumn(name = "fk_column")` for foreign keys

4. **Create Flyway migration** in `src/main/resources/db/migration/`
   - Naming: `V1__create_table_name.sql`, `V2__add_column.sql`

## Example

```java
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"transactions", "user"})
public class Account extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();
}
```
