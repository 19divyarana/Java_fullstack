# REST API (Spring Boot) — Interview-Ready Master Guide

> Covers: notes, real-world examples, complete code, 30–40 interview Q&A, coding problems, AI-style questions, and a mock interview transcript.

---

## PART 1: CORE CONCEPTS — NOTES

### 1.1 Controller

The **Controller** is the entry point for HTTP requests — it receives the request, delegates the actual work to a Service, and shapes the response. In Spring Boot it's a `@RestController`, which combines `@Controller` (marks the class as a request handler) with `@ResponseBody` (return values are serialized straight into the HTTP response body — typically JSON — instead of being resolved as a view template name).

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmployeeDto> getAll() {
        return service.getAll();
    }
}
```

A controller should be **thin** — it validates input shape, calls the service, and maps the result to an HTTP response (status code + body). Business logic does not belong here; that's the service's job. This separation is what makes controllers easy to test (or not even worth unit-testing directly — integration tests cover them) and easy to reuse across different transport layers if needed.

### 1.2 Service

The **Service** layer holds business logic — validation rules, calculations, orchestration across multiple repositories, transaction boundaries. It's annotated `@Service` (a specialization of `@Component`, so it's picked up by component scanning) and is where `@Transactional` typically lives.

```java
@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmployeeDto create(EmployeeDto dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new DuplicateEmailException(dto.email());
        }
        Employee saved = repository.save(EmployeeMapper.toEntity(dto));
        return EmployeeMapper.toDto(saved);
    }
}
```

Why the extra layer instead of calling the repository directly from the controller? Because business rules (duplicate checks, calculations, calling other services, sending events) don't belong in a class whose real job is translating HTTP ↔ Java objects — keeping that logic separate means it's reusable (e.g., by a scheduled job or a message-queue consumer, not just an HTTP call) and independently testable.

### 1.3 Repository

The **Repository** is the data-access layer — it talks to the database and nothing else. Spring Data JPA lets you get a full CRUD implementation for free just by declaring an interface:

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
    List<Employee> findByDepartment(String department);

    @Query("SELECT e FROM Employee e WHERE e.salary > :min")
    List<Employee> findHighEarners(@Param("min") BigDecimal min);
}
```

`JpaRepository<Employee, Long>` already provides `save()`, `findById()`, `findAll()`, `deleteById()`, `count()`, and more — no implementation code needed. Spring Data JPA generates the implementation at runtime, either by parsing the method name (**query derivation** — `findByDepartment` becomes `WHERE department = ?`) or from an explicit `@Query`.

### 1.4 DTO (Data Transfer Object)

A **DTO** is a plain object shaped specifically for what crosses the API boundary — request and response payloads — as opposed to the `@Entity`, which is shaped for how data is stored in the database. They're deliberately kept separate:

```java
// What the API exposes — no internal fields like createdAt, version, or lazy associations leaking out
public record EmployeeDto(Long id, String name, String email, String department, BigDecimal salary) {}

// A narrower DTO for creation — the client shouldn't be able to set the ID
public record CreateEmployeeRequest(
    @NotBlank String name,
    @Email String email,
    @NotBlank String department,
    @Positive BigDecimal salary
) {}
```

**Why not just return the entity directly?** A few concrete reasons: it prevents accidentally serializing lazy-loaded associations (which can throw `LazyInitializationException` or trigger unwanted N+1 queries during JSON serialization); it decouples your public API contract from your internal database schema, so you can change the schema without breaking API consumers; it lets you hide sensitive fields (password hashes, internal audit columns); and it lets the *create* request shape differ from the *response* shape (e.g., clients shouldn't be able to set `id` or `createdAt`).

### 1.5 Entity

The **Entity** is the JPA-mapped class representing a database table — covered in depth in the SQL/Hibernate guide, but the essentials:

```java
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String department;
    private BigDecimal salary;

    // getters, setters, no-arg constructor
}
```

Entities should generally **never** be returned directly from a controller — always map to a DTO first (manually, or with a mapping library like MapStruct).

### 1.6 CRUD APIs

CRUD maps cleanly onto REST + HTTP verbs:

| Operation | HTTP Verb | Example URL | Typical Success Status |
|---|---|---|---|
| Create | `POST` | `/api/employees` | `201 Created` |
| Read (one) | `GET` | `/api/employees/{id}` | `200 OK` |
| Read (all/list) | `GET` | `/api/employees` | `200 OK` |
| Update (full replace) | `PUT` | `/api/employees/{id}` | `200 OK` |
| Update (partial) | `PATCH` | `/api/employees/{id}` | `200 OK` |
| Delete | `DELETE` | `/api/employees/{id}` | `204 No Content` |

Good REST design conventions: URLs are **nouns**, not verbs (`/api/employees`, not `/api/getEmployees`); resource identifiers go in the path (`/employees/{id}`), filters/pagination go in query params (`/employees?department=Sales&page=0`); a successful `POST` returns `201 Created` with a `Location` header pointing at the new resource; a successful `DELETE` returns `204 No Content` (no body, since there's nothing left to return).

### 1.7 Postman

**Postman** is a GUI tool for manually building and sending HTTP requests to test an API — setting the method, URL, headers, auth, and body without writing any client code. Typical workflow when developing an API:

1. Set the method (`POST`) and URL (`http://localhost:8080/api/employees`).
2. Under **Body → raw → JSON**, provide the payload: `{"name": "Alice", "email": "alice@co.com", "department": "Engineering", "salary": 90000}`.
3. Send, then inspect the response status code, headers, and body.
4. Save requests into a **Collection** so the whole team (or CI, via Newman — Postman's CLI runner) can re-run the same set of checks.
5. **Environments** let you swap the base URL (`localhost:8080` in dev, a staging URL in QA) without editing every saved request.

Postman is for manual/exploratory testing and quick debugging during development — it complements, but doesn't replace, automated tests (`@SpringBootTest`/`MockMvc`) that run in CI.

### 1.8 HTTP Methods

| Method | Purpose | Idempotent? | Has a body? |
|---|---|---|---|
| `GET` | Retrieve a resource | Yes | No |
| `POST` | Create a new resource | No | Yes |
| `PUT` | Replace a resource entirely | Yes | Yes |
| `PATCH` | Partially update a resource | No (in practice, often treated as not guaranteed) | Yes |
| `DELETE` | Remove a resource | Yes | Usually no |

**Idempotent** means calling it multiple times has the same effect as calling it once. `GET`, `PUT`, and `DELETE` are idempotent — sending the same `DELETE /employees/5` request five times still results in employee 5 being gone (the first call deletes it, the rest are no-ops, typically returning `404` on repeats). `POST` is **not** idempotent — calling `POST /employees` with the same body twice creates two separate employees, which is exactly why retry logic on `POST` requests is dangerous without an idempotency key.

### 1.9 Status Codes

| Code | Meaning | Typical Use |
|---|---|---|
| `200 OK` | Success | Successful `GET`/`PUT`/`PATCH` |
| `201 Created` | Resource created | Successful `POST` |
| `204 No Content` | Success, nothing to return | Successful `DELETE` |
| `400 Bad Request` | Client sent invalid data | Failed `@Valid` validation, malformed JSON |
| `401 Unauthorized` | Missing/invalid authentication | No/expired token |
| `403 Forbidden` | Authenticated, but not allowed | Valid token, insufficient role/permission |
| `404 Not Found` | Resource doesn't exist | `GET /employees/9999` when 9999 doesn't exist |
| `409 Conflict` | Request conflicts with current state | Duplicate email on create |
| `422 Unprocessable Entity` | Well-formed but semantically invalid | Business-rule validation failure |
| `500 Internal Server Error` | Unhandled server-side failure | Uncaught exception, bug |

**401 vs 403, the classic mix-up**: 401 means "I don't know who you are" (no valid credentials at all); 403 means "I know who you are, but you're not allowed to do this" (valid credentials, insufficient permission).

---

## PART 2: CODING — COMPLETE EMPLOYEE CRUD API (💻)

Full, runnable Spring Boot REST API with proper layering (Controller → Service → Repository), DTOs, validation, exception handling, and correct status codes.

**`pom.xml`** (relevant dependencies):
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**`Employee.java`** (entity):
```java
package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String department;
    private BigDecimal salary;

    public Employee() {}

    public Employee(String name, String email, String department, BigDecimal salary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
    }

    // getters and setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
}
```

**DTOs** — `EmployeeDto.java`, `CreateEmployeeRequest.java`, `UpdateEmployeeRequest.java`:
```java
package com.example.demo.dto;

import java.math.BigDecimal;

public record EmployeeDto(Long id, String name, String email, String department, BigDecimal salary) {}
```
```java
package com.example.demo.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateEmployeeRequest(
    @NotBlank(message = "Name is required") String name,
    @Email(message = "Email must be valid") @NotBlank String email,
    @NotBlank(message = "Department is required") String department,
    @Positive(message = "Salary must be positive") BigDecimal salary
) {}
```
```java
package com.example.demo.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record UpdateEmployeeRequest(String name, String department, @Positive BigDecimal salary) {}
```

**`EmployeeMapper.java`**:
```java
package com.example.demo.dto;

import com.example.demo.model.Employee;

public class EmployeeMapper {
    public static EmployeeDto toDto(Employee e) {
        return new EmployeeDto(e.getId(), e.getName(), e.getEmail(), e.getDepartment(), e.getSalary());
    }

    public static Employee toEntity(CreateEmployeeRequest req) {
        return new Employee(req.name(), req.email(), req.department(), req.salary());
    }
}
```

**`EmployeeRepository.java`**:
```java
package com.example.demo.repository;

import com.example.demo.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
    List<Employee> findByDepartment(String department);
}
```

**Custom exceptions** — `EmployeeNotFoundException.java`, `DuplicateEmailException.java`:
```java
package com.example.demo.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id: " + id);
    }
}
```
```java
package com.example.demo.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Employee already exists with email: " + email);
    }
}
```

**`EmployeeService.java`**:
```java
package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.exception.*;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmployeeDto create(CreateEmployeeRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        Employee saved = repository.save(EmployeeMapper.toEntity(request));
        return EmployeeMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getById(Long id) {
        Employee employee = repository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        return EmployeeMapper.toDto(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getAll() {
        return repository.findAll().stream().map(EmployeeMapper::toDto).toList();
    }

    @Transactional
    public EmployeeDto update(Long id, UpdateEmployeeRequest request) {
        Employee employee = repository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));

        if (request.name() != null) employee.setName(request.name());
        if (request.department() != null) employee.setDepartment(request.department());
        if (request.salary() != null) employee.setSalary(request.salary());

        return EmployeeMapper.toDto(employee); // dirty checking persists the change on commit
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
```

**`EmployeeController.java`**:
```java
package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeDto created = service.create(request);
        return ResponseEntity
            .created(URI.create("/api/employees/" + created.id()))
            .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**`GlobalExceptionHandler.java`** — centralizes error → status code mapping:
```java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EmployeeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        Map<String, Object> body = errorBody("Validation failed");
        body.put("fields", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", message);
        return body;
    }
}
```

**Example Postman/curl calls:**
```bash
# Create
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@co.com","department":"Engineering","salary":90000}'
# -> 201 Created, Location: /api/employees/1

# Get one
curl http://localhost:8080/api/employees/1
# -> 200 OK

# Get all
curl http://localhost:8080/api/employees
# -> 200 OK

# Update
curl -X PUT http://localhost:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Smith","department":"Engineering","salary":95000}'
# -> 200 OK

# Delete
curl -X DELETE http://localhost:8080/api/employees/1
# -> 204 No Content

# Get a non-existent employee
curl http://localhost:8080/api/employees/999
# -> 404 Not Found, {"timestamp":"...","message":"Employee not found with id: 999"}
```

---

## PART 3: REAL-WORLD EXAMPLES (💼)

1. **API versioning under a breaking change**: an e-commerce team needs to change the shape of `ProductDto` (splitting `price` into `basePrice` + `tax`) without breaking existing mobile clients — handled by introducing `/api/v2/products` alongside the existing `/api/v1/products`, rather than mutating the existing contract, because DTOs (not entities) are what's actually versioned at the API boundary.

2. **Idempotency keys for payment POSTs**: a checkout API accepts an `Idempotency-Key` header on `POST /payments` so that a client's network retry after a timeout doesn't double-charge the customer — since `POST` is not idempotent by default, the server explicitly tracks keys it's already processed and returns the original result instead of creating a second charge.

3. **Pagination for a large employee directory**: `GET /api/employees?page=0&size=20&sort=name,asc` using Spring Data's `Pageable`, returning metadata (`totalElements`, `totalPages`) alongside the page of results — returning all 50,000 employees in one `GET /employees` call would be a real production incident (slow response, huge payload, potential OOM on the client).

4. **Postman collections in CI**: teams export a Postman collection and run it headlessly via **Newman** in a CI pipeline as a smoke test after every deploy — hitting `/health`, then a couple of representative CRUD endpoints, to catch a broken deployment before real users do.

5. **Distinguishing 401 vs 403 in a real incident**: a mobile app support ticket says "I keep getting logged out," but the actual API response was `403 Forbidden`, not `401 Unauthorized` — meaning the token was valid but the user's role lacked permission for that specific action; conflating the two in logs/monitoring dashboards is a common real debugging trap that wastes time chasing the wrong root cause (auth vs. authorization).

6. **PATCH vs PUT for a mobile app with poor connectivity**: a profile-editing screen uses `PATCH` to send only the one field the user actually changed, rather than `PUT` with the full object — reducing payload size and avoiding accidentally overwriting fields the client's local cache happened to be stale on.

---

## PART 4: INTERVIEW QUESTIONS (❓ 35 Q&A)

**1. What's the role of a Controller in a Spring Boot REST API?**
It's the HTTP entry point — receives requests, delegates business logic to the service layer, and maps the result to an HTTP response (status code + body). It should stay thin, with no business logic of its own.

**2. Why separate Controller, Service, and Repository into distinct layers?**
Separation of concerns: the controller handles HTTP translation, the service holds business logic and transaction boundaries, the repository handles data access. This makes each layer independently testable and reusable (e.g., the service can be called from a scheduled job, not just HTTP).

**3. What is a DTO, and why not just return the entity directly from a controller?**
A DTO is an object shaped for the API boundary. Returning entities directly risks leaking lazy-loaded associations (LazyInitializationException, N+1 queries during serialization), couples your public API to your DB schema, and exposes internal-only fields.

**4. What's the difference between an Entity and a DTO?**
An Entity is mapped to a database table via JPA annotations and represents persistence structure; a DTO represents the API's request/response contract and can be shaped completely differently (fewer fields, combined fields, validation annotations) from the entity.

**5. Why might a create-request DTO differ from a response DTO for the same resource?**
The client shouldn't be able to set server-generated fields like `id` or `createdAt` on creation, so `CreateEmployeeRequest` intentionally omits them, while `EmployeeDto` (the response) includes the generated `id`.

**6. What does `@RequestMapping` (and its HTTP-verb-specific variants) do?**
Maps incoming HTTP requests to controller methods based on URL path and HTTP method; `@GetMapping`, `@PostMapping`, etc. are shorthand for `@RequestMapping(method = ...)`.

**7. What's the difference between `@PathVariable` and `@RequestParam`?**
`@PathVariable` extracts a value from the URL path itself (`/employees/{id}`); `@RequestParam` extracts a query-string parameter (`/employees?department=Sales`).

**8. What does `@RequestBody` do, and what performs the JSON-to-object conversion?**
Binds the HTTP request body to a Java object parameter; Spring uses a registered `HttpMessageConverter` (Jackson, by default) to deserialize JSON into that object.

**9. What is `@Valid` used for, and what happens when validation fails?**
Triggers Bean Validation (JSR 380) constraints declared on the DTO (`@NotBlank`, `@Email`, `@Positive`, etc.); on failure, Spring throws `MethodArgumentNotValidException`, typically caught by a `@RestControllerAdvice` and translated into a `400 Bad Request` with field-level error details.

**10. What is `@RestControllerAdvice` and why use it instead of try/catch in every controller method?**
It centralizes exception-to-HTTP-response mapping across all controllers in one place, avoiding repetitive try/catch blocks scattered through every endpoint and keeping error-response formatting consistent app-wide.

**11. Explain the difference between PUT and PATCH.**
`PUT` replaces the entire resource with the provided representation (fields not included are typically treated as unset/nulled); `PATCH` applies a partial update, changing only the fields provided.

**12. Why is PUT considered idempotent but POST is not?**
Calling `PUT /employees/5` with the same body repeatedly leaves employee 5 in the same end state every time. Calling `POST /employees` repeatedly creates a new resource each time — five identical calls produce five different employees.

**13. What status code should a successful POST that creates a resource return, and what header should accompany it?**
`201 Created`, along with a `Location` header pointing to the URL of the newly created resource.

**14. What status code should a successful DELETE return, and why no body?**
`204 No Content` — there's nothing meaningful left to return since the resource no longer exists; some APIs alternatively return `200 OK` with a confirmation body, but `204` is the more RESTful convention.

**15. What's the difference between 401 and 403?**
401 means the request lacks valid authentication (server doesn't know who you are); 403 means authentication succeeded but the authenticated user isn't authorized for this specific action.

**16. When would you use 409 Conflict vs 400 Bad Request?**
400 is for malformed/invalid request data (wrong types, missing required fields); 409 is for a syntactically valid request that conflicts with the current state of the resource (e.g., creating an employee with an email that already exists).

**17. What is `ResponseEntity`, and why use it instead of returning the DTO directly from a controller method?**
`ResponseEntity<T>` wraps the response body together with the HTTP status code and headers, giving explicit control (e.g., `201` with a `Location` header on create) rather than always defaulting to `200 OK`.

**18. What does Spring do automatically for a method that just returns a DTO (not wrapped in `ResponseEntity`) from a `@RestController`?**
It serializes the return value into the response body via Jackson and defaults to `200 OK` (or `204` for a `void` method, depending on context) — you lose the ability to easily customize the status code or headers without switching to `ResponseEntity`.

**19. How would you implement pagination in a Spring Boot REST API?**
Accept a `Pageable` parameter in the controller method (bound from `?page=`, `?size=`, `?sort=` query params) and use a repository method returning `Page<Employee>`; the response naturally includes total elements/pages metadata alongside the current page's content.

**20. What's the purpose of API versioning, and name two common approaches.**
Lets you evolve/break an API contract without breaking existing clients still relying on the old shape. Common approaches: URL versioning (`/api/v1/...`, `/api/v2/...`) or header-based versioning (`Accept: application/vnd.company.v2+json`).

**21. What is HATEOAS, briefly?**
"Hypermedia as the Engine of Application State" — responses include links to related actions/resources (e.g., a `self` link, a `next-page` link), letting clients navigate the API dynamically rather than hardcoding URLs. Rarely fully implemented in practice, but commonly asked conceptually.

**22. What's the difference between a repository method using query derivation vs. `@Query`?**
Query derivation (`findByDepartment`) generates the query automatically by parsing the method name; `@Query` lets you write explicit JPQL/native SQL for cases too complex for name-based derivation to express cleanly.

**23. Why annotate a service method with `@Transactional` rather than the repository or controller?**
The service layer is where a business operation's boundaries are defined — often spanning multiple repository calls that must succeed or fail together. Repositories are too granular (a single operation), and controllers shouldn't own transaction semantics, which are a business/data concern, not an HTTP concern.

**24. What would happen if you put `@Transactional` on a controller method instead of the service?**
It would technically work in simple cases, but it's a layering violation — it ties transaction boundaries to the HTTP layer, making the same business logic non-reusable (and non-transactional) if called from anywhere other than an HTTP request, e.g., a scheduled job or message consumer.

**25. How do you test a REST controller without starting a full embedded server?**
`@WebMvcTest` combined with `MockMvc` — loads just the web layer (controller + related config), with the service layer typically mocked via `@MockBean`, letting you assert on status codes, response bodies, and headers without a real HTTP server or database.

**26. What's the difference between `@WebMvcTest` and `@SpringBootTest`?**
`@WebMvcTest` loads only the web layer (fast, focused); `@SpringBootTest` boots the entire application context (slower, used for true end-to-end integration tests).

**27. How would you handle a scenario where a client sends a malformed JSON body?**
Spring throws `HttpMessageNotReadableException` before your controller method even runs (deserialization fails); catch it in your `@RestControllerAdvice` and map it to `400 Bad Request` with a clear error message.

**28. What's an idempotency key, and when is it needed?**
A client-generated unique identifier sent with a non-idempotent request (typically `POST`) so the server can detect and safely ignore a retried duplicate request — commonly used in payment APIs where a network-timeout retry must not cause a double-charge.

**29. Why might a `GET` request with side effects (e.g., incrementing a view counter) be considered bad REST design?**
`GET` is expected to be safe (no server-side state change) and idempotent/cacheable; a `GET` with side effects breaks caching assumptions, can be triggered accidentally by prefetching/crawlers, and violates the semantic contract clients and intermediaries (proxies, browsers) rely on.

**30. What's the purpose of `existsByEmail()` in the repository, versus just calling `findByEmail()` and checking for null?**
`existsByEmail()` can be translated into a lighter `SELECT 1 ... LIMIT 1`/`EXISTS` style query by Spring Data, avoiding fetching and materializing a full entity just to check existence.

**31. How would you avoid exposing a stack trace to API clients on an unhandled exception?**
A catch-all `@ExceptionHandler(Exception.class)` in the `@RestControllerAdvice` that logs the full exception server-side but returns a generic `500 Internal Server Error` body to the client, without leaking internal details like class names or SQL.

**32. What does `Content-Type: application/json` in a request header actually do?**
Tells the server how to interpret the request body's bytes, so Spring picks the correct `HttpMessageConverter` (Jackson for JSON) to deserialize it into the target Java object.

**33. What's the difference between `Accept` and `Content-Type` headers?**
`Content-Type` describes the format of the data being *sent* (the request body); `Accept` tells the server what format the client wants the *response* in.

**34. How would you design an endpoint to search/filter employees by multiple optional criteria (department, min salary)?**
`GET /api/employees?department=Sales&minSalary=50000` with each query param optional (`@RequestParam(required = false)`), building the query dynamically — e.g., via Spring Data JPA Specifications or `Criteria` API when the combination of filters is open-ended.

**35. Why does Postman's "Environment" feature matter for a team, beyond individual convenience?**
It lets the exact same saved collection of requests run against dev, staging, and prod by swapping one base-URL variable, instead of every team member maintaining separate hardcoded copies of each request per environment — reducing drift and copy-paste mistakes.

---

## PART 5: CODING PROBLEMS (🧩)

### Problem 1 — Add Pagination to the "Get All Employees" Endpoint
**Task**: rewrite `getAll()` to support pagination instead of returning every employee at once.

```java
// Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findAll(Pageable pageable);   // already inherited, shown for clarity
}

// Controller
@GetMapping
public ResponseEntity<Page<EmployeeDto>> getAll(Pageable pageable) {
    Page<EmployeeDto> page = repository.findAll(pageable).map(EmployeeMapper::toDto);
    return ResponseEntity.ok(page);
}
```
`GET /api/employees?page=0&size=20&sort=name,asc` now works out of the box.

### Problem 2 — Fix a Non-Idempotent DELETE
**Given**: `DELETE /employees/5` currently returns `404` on the *first* call because of a bug, but should return `204` the first time and `404` only on repeat calls (since the resource is genuinely gone by then). Identify the bug.
```java
// BUGGY
@Transactional
public void delete(Long id) {
    repository.deleteById(id);  // deleteById() silently no-ops if the row doesn't exist —
                                  // this method never actually checks existence first
}
```
```java
// FIXED
@Transactional
public void delete(Long id) {
    if (!repository.existsById(id)) {
        throw new EmployeeNotFoundException(id);   // now correctly 404s only when actually missing
    }
    repository.deleteById(id);
}
```

### Problem 3 — Enforce Validation on a Nested Object
**Task**: `CreateEmployeeRequest` now includes a nested `AddressDto`. Make sure its fields are validated too.
```java
public record AddressDto(@NotBlank String city, @NotBlank String zip) {}

public record CreateEmployeeRequest(
    @NotBlank String name,
    @Email String email,
    @Valid AddressDto address   // @Valid must be repeated here — validation doesn't cascade automatically
) {}
```

### Problem 4 — Return the Correct Status Code for a Duplicate Resource
**Given**: creating an employee with an email that already exists currently returns `500 Internal Server Error` because the unique-constraint violation isn't handled. Fix it properly.
```java
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", "A resource with these unique fields already exists");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);   // 409, not 500
}
```
(Better still: check `existsByEmail()` proactively in the service, as shown in Part 2, so the conflict is caught with a clear message before ever hitting the database constraint.)

### Problem 5 — Implement a PATCH Endpoint That Only Updates Provided Fields
**Task**: given `UpdateEmployeeRequest(String name, String department, BigDecimal salary)` where any field may be `null` (meaning "don't change this"), write the PATCH handler.
```java
@PatchMapping("/{id}")
public ResponseEntity<EmployeeDto> patch(@PathVariable Long id, @RequestBody UpdateEmployeeRequest request) {
    return ResponseEntity.ok(service.update(id, request));  // service.update() already null-checks each field
}
```

### Problem 6 — Prevent an Idempotency Bug on Retried POSTs
**Task**: sketch how you'd add idempotency-key support to the create-employee endpoint.
```java
@PostMapping
public ResponseEntity<EmployeeDto> create(
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody CreateEmployeeRequest request) {

    if (idempotencyKey != null) {
        Optional<EmployeeDto> existing = idempotencyStore.get(idempotencyKey);
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(existing.get()); // return the original result, no duplicate
        }
    }
    EmployeeDto created = service.create(request);
    if (idempotencyKey != null) {
        idempotencyStore.put(idempotencyKey, created);
    }
    return ResponseEntity.created(URI.create("/api/employees/" + created.id())).body(created);
}
```

---

## PART 6: AI-STYLE INTERVIEW QUESTIONS (🤖)

**1. "A frontend team complains your API's PUT endpoint keeps wiping fields they didn't intend to change. What's actually going on, and how do you resolve it without breaking REST semantics?"**
Expected reasoning: `PUT` is defined as a full replace — fields omitted from the request body are correctly treated as "unset," so a frontend sending a partial object via `PUT` will lose data by design, not by bug. The fix isn't to make `PUT` secretly partial (that breaks the contract for other clients relying on real PUT semantics) — it's to add a proper `PATCH` endpoint for partial updates and have the frontend use that instead.

**2. "You're asked to add a `GET` endpoint that also logs an audit event and increments a 'views' counter each time it's called. Any concerns?"**
Expected reasoning: `GET` is supposed to be safe and side-effect-free from the client's perspective; a view counter is a debatable but common exception (arguably a side effect the client doesn't care about), but concerns include: browsers/crawlers/link-preview bots prefetching the URL and inflating the counter falsely, caching layers or CDNs serving cached responses and skipping the counter increment inconsistently, and retries/proxies calling `GET` multiple times for a single logical user action. Worth flagging these trade-offs rather than silently implementing it.

**3. "Your team debates whether `DELETE /employees/5` should return `404` or `204` on a second call after the first successfully deleted it. Which is 'more correct,' and does it actually matter?"**
Expected reasoning: `404` is the technically defensible answer (the resource genuinely doesn't exist anymore), and matches idempotency correctly — repeated calls converge to the same *end state* (deleted), even though the *response code* differs between the first and later calls, which is fine since idempotency is about resource state, not response code identity. Practically, whichever choice is made, the important thing is documenting it clearly and being consistent across the API, since client retry logic may depend on it.

**4. "An AI coding assistant generated a controller method that returns the `Employee` entity directly instead of a DTO. What's the actual risk, not just the style objection?"**
Expected reasoning: beyond style, this is a real risk — if `Employee` has a lazy `@OneToMany` association, Jackson serializing it inside an open transaction can trigger unwanted N+1 queries per request, or a `LazyInitializationException` if serialization happens outside the transaction boundary; it also silently couples the public API contract to internal schema changes, and can leak fields never meant to be public.

**5. "How would you decide whether a new filter capability on `GET /employees` should be a new endpoint (`/employees/high-earners`) or a query parameter (`?minSalary=X`)?"**
Expected reasoning: a query parameter is more RESTful and composable when the filter is one of several optional, combinable criteria on the same resource collection; a dedicated endpoint makes sense when the result represents a genuinely distinct, well-known concept with its own semantics/caching/documentation needs (e.g., `/employees/on-leave` as a recognized business concept, not just an arbitrary filter). Default to query params unless there's a strong reason otherwise.

**6. "Your idempotency-key implementation for POST stores keys in an in-memory map. What breaks in production, and why might that not be obvious in testing?"**
Expected reasoning: an in-memory map doesn't survive a restart or scale across multiple instances behind a load balancer — a retried request could hit a different instance than the original and not find the key, causing the exact double-processing the mechanism was meant to prevent. This is invisible in a single-instance local test environment, which is exactly why it's a dangerous class of bug — it passes tests and fails specifically under production-like horizontal scaling.

**7. "Someone proposes returning `200 OK` with a JSON body like `{\"success\": false, \"error\": \"not found\"}` instead of using proper HTTP status codes. What's your take?"**
Expected reasoning: this pattern (sometimes called "200 OK everything") throws away a lot of value for free — HTTP clients, proxies, monitoring/alerting tools, and caching layers all key off status codes automatically; forcing every consumer to parse the body just to know if a call succeeded adds work and removes standard tooling compatibility, for no real benefit over just using the status code correctly.

**8. "A teammate wants to skip the service layer entirely for a 'simple' CRUD endpoint and call the repository directly from the controller, to reduce boilerplate. Reasonable?"**
Expected reasoning: for a truly trivial case it might work today, but it erodes the layering convention the rest of the codebase relies on, and "simple" endpoints have a way of acquiring business rules later (validation, duplicate checks, events) — at which point the logic either gets awkwardly bolted onto the controller or requires a refactor anyway. Weigh consistency and future-proofing against the marginal boilerplate saved; for a codebase-wide convention, consistency usually wins.

---

## PART 7: MOCK INTERVIEW (🎤)

*A realistic back-and-forth transcript.*

**Interviewer:** Let's start with the basics. Walk me through what happens, layer by layer, when a client sends `POST /api/employees` with a JSON body.

**You:** The request hits Spring's `DispatcherServlet` first, which routes it to the matching controller method based on the URL and HTTP verb — in this case, the `create` method in `EmployeeController`. Spring deserializes the JSON body into a `CreateEmployeeRequest` DTO via Jackson, and if it's annotated with `@Valid`, Bean Validation runs against it — things like `@NotBlank`, `@Email`. If that passes, the controller calls into the service layer, which holds the actual business logic — in this case checking whether the email is already taken. If it's not, the service maps the DTO into an `Employee` entity and saves it via the repository, which is where Spring Data JPA actually talks to the database. The saved entity comes back up, gets mapped to a response DTO, and the controller wraps it in a `ResponseEntity` with a `201 Created` status and a `Location` header pointing at the new resource.

**Interviewer:** Good, that's the full path. Let's dig into one part — why bother with a DTO at all here instead of just accepting and returning the `Employee` entity directly?

**You:** A few concrete reasons, not just style preference. If `Employee` has any lazy-loaded associations, serializing it directly can either throw a `LazyInitializationException` if the session's already closed, or quietly trigger extra queries per request if it's still open — neither is something you want happening implicitly during JSON serialization. It also ties your public API contract directly to your database schema, so a schema change becomes a breaking API change. And for the create request specifically, the DTO lets me leave out fields like `id` that the client shouldn't be allowed to set themselves.

**Interviewer:** Makes sense. Now, a design question — why PUT for full update and PATCH for partial? Why not just have PUT handle both?

**You:** Because that breaks the actual semantics clients and tooling rely on. PUT means "here's the complete new state of this resource" — if I send a partial object via PUT, the standard interpretation is that the missing fields are being unset, not left alone. If I want "update only what I send," that's specifically what PATCH means. Blurring the two either surprises clients who expect standard PUT behavior, or forces every client to always send the full object even for a one-field change, which is wasteful, especially on something like a mobile connection.

**Interviewer:** Let's talk error handling. Suppose two requests to create an employee with the same email arrive back to back. What actually happens, and what should the client see?

**You:** Ideally the service layer catches this proactively — I'd have it call something like `existsByEmail()` before attempting the save, and if that's true, throw a specific `DuplicateEmailException`. That gets caught centrally in a `@RestControllerAdvice`, which maps it to a `409 Conflict` with a clear message. If for some reason both requests raced past that check at the same time, the database's own unique constraint would reject the second insert, so I'd also want a fallback handler for `DataIntegrityViolationException`, mapped to the same `409`, so the client never sees a raw `500` for what's fundamentally a normal, expected conflict.

**Interviewer:** Good — you clearly thought about the race condition, not just the happy path. Last question, and it's open-ended: how would you explain to a non-technical stakeholder why `POST` retries are risky but `GET`/`PUT`/`DELETE` retries generally aren't?

**You:** I'd frame it around the idea of "doing it again causing something new to happen" versus "doing it again landing you in the same place." If your app's network hiccups and it silently retries a `GET`, you just re-read the same data — no harm. Same with `DELETE` — trying to delete something that's already deleted just tells you it's not there anymore, nothing changes. But `POST` means "create a new thing" — if I click "place order" and the request silently retries because of a bad connection, I might end up with two separate orders, two charges, without ever clicking twice myself. That's exactly the class of bug idempotency keys exist to prevent, and it's worth investing in specifically for anything involving money or anything that shouldn't happen twice.

**Interviewer:** That's a genuinely clear explanation — good instinct for translating a technical concept for a non-technical audience. Thanks, that's everything I wanted to cover.

---

*End of guide.*
