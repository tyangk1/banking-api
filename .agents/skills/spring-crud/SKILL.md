---
name: spring-crud
description: Guide to generating a full CRUD stack for the Banking API
---

# Spring CRUD Generation Skill

Use this skill whenever creating a full CRUD feature for a new resource.

## Generation Order

Always create files in this order (dependency-first):

1. **Entity** → `model/entity/Xxx.java` (follow `spring-entity` skill)
2. **Enums** → `model/enums/XxxStatus.java`, `model/enums/XxxType.java`
3. **Repository** → `repository/XxxRepository.java`
4. **DTOs** → `model/dto/request/XxxRequest.java`, `model/dto/response/XxxResponse.java`
5. **Mapper** → `model/mapper/XxxMapper.java`
6. **Service** → `service/XxxService.java` (interface) + `service/impl/XxxServiceImpl.java`
7. **Controller** → `controller/XxxController.java`
8. **Tests** → `service/XxxServiceTest.java`, `controller/XxxControllerTest.java`

## Templates

### Repository
```java
@Repository
public interface XxxRepository extends JpaRepository<Xxx, String> {
    Optional<Xxx> findByFieldName(String fieldName);
    Page<Xxx> findByStatus(XxxStatus status, Pageable pageable);
    boolean existsByFieldName(String fieldName);
}
```

### Request DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create/update Xxx")
public class CreateXxxRequest {

    @NotBlank(message = "Field is required")
    @Schema(description = "Description", example = "example")
    private String field;
}
```

### Response DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Xxx response")
public class XxxResponse {
    @Schema(description = "Unique ID", example = "uuid-string")
    private String id;
    // mapped fields...
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### MapStruct Mapper
```java
@Mapper(componentModel = "spring")
public interface XxxMapper {
    XxxResponse toResponse(Xxx entity);
    List<XxxResponse> toResponseList(List<Xxx> entities);
    Xxx toEntity(CreateXxxRequest request);
    void updateEntity(UpdateXxxRequest request, @MappingTarget Xxx entity);
}
```

### Service Interface
```java
public interface XxxService {
    XxxResponse create(CreateXxxRequest request);
    XxxResponse getById(String id);
    Page<XxxResponse> getAll(Pageable pageable);
    XxxResponse update(String id, UpdateXxxRequest request);
    void delete(String id);
}
```

### Service Implementation
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class XxxServiceImpl implements XxxService {

    private final XxxRepository xxxRepository;
    private final XxxMapper xxxMapper;

    @Override
    @Transactional
    public XxxResponse create(CreateXxxRequest request) {
        // 1. Validate business rules
        // 2. Map to entity
        // 3. Save
        // 4. Return response
    }

    @Override
    @Transactional(readOnly = true)
    public XxxResponse getById(String id) {
        Xxx entity = xxxRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Xxx", "id", id));
        return xxxMapper.toResponse(entity);
    }
}
```

### Controller
```java
@RestController
@RequestMapping("/api/v1/xxxs")
@RequiredArgsConstructor
@Tag(name = "Xxx Management", description = "APIs for managing Xxx resources")
@Slf4j
public class XxxController {

    private final XxxService xxxService;

    @PostMapping
    @Operation(summary = "Create new Xxx")
    public ResponseEntity<ApiResponse<XxxResponse>> create(
            @Valid @RequestBody CreateXxxRequest request) {
        XxxResponse response = xxxService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Xxx created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Xxx by ID")
    public ResponseEntity<ApiResponse<XxxResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(xxxService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all Xxx (paginated)")
    public ResponseEntity<ApiResponse<Page<XxxResponse>>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(xxxService.getAll(pageable)));
    }
}
```
