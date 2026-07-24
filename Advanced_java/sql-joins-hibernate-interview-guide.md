# SQL Joins + Hibernate — Interview-Ready Master Guide

> Covers: notes, internal working, real-world examples, complete code, 30–40 interview Q&A, coding problems, AI-style questions, and a mock interview transcript.

---

## PART 1: SQL JOINS

### 1.1 Interview-Ready Notes

**Sample schema used throughout this section:**

```sql
CREATE TABLE department (
    dept_id   INT PRIMARY KEY,
    dept_name VARCHAR(50)
);

CREATE TABLE employee (
    emp_id    INT PRIMARY KEY,
    emp_name  VARCHAR(50),
    dept_id   INT,
    salary    DECIMAL(10,2),
    FOREIGN KEY (dept_id) REFERENCES department(dept_id)
);

INSERT INTO department VALUES (1,'Engineering'), (2,'Sales'), (3,'HR'), (4,'Marketing');

INSERT INTO employee VALUES
(101,'Alice',1,90000),
(102,'Bob',1,85000),
(103,'Carol',2,70000),
(104,'Dan',NULL,60000),
(105,'Eve',3,75000);
```

Note: department 4 (Marketing) has no employees. Employee 104 (Dan) has no department (`dept_id = NULL`). These two "orphan" rows are what make joins interesting.

#### INNER JOIN
Returns only rows where the join condition matches in **both** tables. Non-matching rows on either side are dropped.

```sql
SELECT e.emp_name, d.dept_name
FROM employee e
INNER JOIN department d ON e.dept_id = d.dept_id;
```
Result: Alice, Bob, Carol, Eve (Dan and Marketing are excluded — Dan has no dept, Marketing has no employee).

#### LEFT JOIN (LEFT OUTER JOIN)
Returns **all rows from the left table**, plus matched rows from the right table. Unmatched right-side columns come back as `NULL`.

```sql
SELECT e.emp_name, d.dept_name
FROM employee e
LEFT JOIN department d ON e.dept_id = d.dept_id;
```
Result: all 5 employees; Dan's `dept_name` is `NULL`.

#### RIGHT JOIN (RIGHT OUTER JOIN)
Mirror of LEFT JOIN — returns **all rows from the right table**, plus matches from the left. Unmatched left-side columns are `NULL`.

```sql
SELECT e.emp_name, d.dept_name
FROM employee e
RIGHT JOIN department d ON e.dept_id = d.dept_id;
```
Result: all 4 departments; Marketing's `emp_name` is `NULL`. (Note: MySQL supports RIGHT JOIN; Oracle/PostgreSQL do too, but RIGHT JOIN is rarely used in practice — people just swap table order and use LEFT JOIN instead, since it reads more naturally.)

#### FULL JOIN (FULL OUTER JOIN)
Returns everything — matched rows, plus unmatched rows from **both** sides, with `NULL` filling the gaps. Union of LEFT and RIGHT JOIN.

```sql
-- Standard SQL (Postgres, SQL Server, Oracle)
SELECT e.emp_name, d.dept_name
FROM employee e
FULL JOIN department d ON e.dept_id = d.dept_id;

-- MySQL has no FULL JOIN — emulate with UNION
SELECT e.emp_name, d.dept_name
FROM employee e LEFT JOIN department d ON e.dept_id = d.dept_id
UNION
SELECT e.emp_name, d.dept_name
FROM employee e RIGHT JOIN department d ON e.dept_id = d.dept_id;
```
Result: all 5 employees + Marketing, with NULLs on both sides where unmatched.

**Quick comparison table**

| Join Type | Left unmatched rows | Right unmatched rows | Matched rows |
|---|---|---|---|
| INNER | ✗ | ✗ | ✓ |
| LEFT  | ✓ | ✗ | ✓ |
| RIGHT | ✗ | ✓ | ✓ |
| FULL  | ✓ | ✓ | ✓ |

Related, commonly asked alongside joins:
- **CROSS JOIN**: Cartesian product — every row of A paired with every row of B, no ON clause. `SELECT * FROM employee CROSS JOIN department;` → 5 × 4 = 20 rows.
- **SELF JOIN**: a table joined to itself, typically to compare rows (e.g., find employees earning more than their manager). Not a distinct join *type* — just an INNER/LEFT join where both sides reference the same table with aliases.

#### Subqueries
A query nested inside another query. Three common flavors:

1. **Scalar subquery** (returns a single value) — used in `SELECT` or `WHERE`:
```sql
SELECT emp_name, salary
FROM employee
WHERE salary > (SELECT AVG(salary) FROM employee);
```

2. **Correlated subquery** (references the outer query, runs once per outer row):
```sql
SELECT e.emp_name
FROM employee e
WHERE e.salary > (
    SELECT AVG(e2.salary) FROM employee e2 WHERE e2.dept_id = e.dept_id
);
```

3. **Subquery in `FROM`** (derived table) or **`IN`/`EXISTS`**:
```sql
-- IN
SELECT dept_name FROM department
WHERE dept_id IN (SELECT dept_id FROM employee WHERE salary > 80000);

-- EXISTS (often faster than IN for large datasets, short-circuits on first match)
SELECT dept_name FROM department d
WHERE EXISTS (SELECT 1 FROM employee e WHERE e.dept_id = d.dept_id AND e.salary > 80000);
```

**Subquery vs Join — when to use which**: a join is usually preferred when you need columns from both tables in the output and the optimizer can use indexes/hash joins efficiently. A subquery (especially `EXISTS`) is often clearer and faster when you only need to *filter* based on another table's existence, not pull its columns.

---

### 1.2 Internal Working (🧠 how the engine actually executes joins)

The `ON` condition and join *keyword* are just the logical request. The **query optimizer** picks a physical algorithm based on table sizes, indexes, and statistics. The three classic physical join algorithms:

**1. Nested Loop Join**
```
for each row R1 in outer table:
    for each row R2 in inner table:
        if R1.key == R2.key: emit (R1, R2)
```
- Complexity: O(N × M) without an index; O(N × log M) if the inner table has an index on the join key (index nested loop join).
- Best when one table is small, or when an index exists on the inner table's join column.
- This is what typically runs when you join a large table to a small, indexed lookup table (like `employee` → `department`).

**2. Hash Join**
- Build phase: scan the smaller table, build an in-memory hash table keyed on the join column.
- Probe phase: scan the larger table, look up each row's key in the hash table.
- Complexity: roughly O(N + M).
- Used for large, unsorted, unindexed equi-joins (`=` conditions only — hash joins can't do range joins like `<`).
- Needs enough memory to hold the hash table; if not, it spills to disk ("grace hash join").

**3. Sort-Merge Join**
- Sort both tables by the join key (or use an existing sorted index).
- Walk both sorted lists in parallel, like merging two sorted arrays.
- Complexity: O(N log N + M log M) for the sort, O(N + M) for the merge.
- Good for large tables already sorted (or indexed) on the join key, and works for non-equi joins (`<`, `>`) unlike hash join.

**How the optimizer decides**: it uses table statistics (row counts, cardinality, index availability) to estimate the cost of each physical plan and picks the cheapest one. You can inspect the actual chosen plan with `EXPLAIN` (MySQL/Postgres) or `EXPLAIN ANALYZE` for real execution timings.

```sql
EXPLAIN ANALYZE
SELECT e.emp_name, d.dept_name
FROM employee e
INNER JOIN department d ON e.dept_id = d.dept_id;
```

**LEFT/RIGHT/FULL internally**: outer joins are implemented as the same physical algorithms above, just with an extra step: after the match phase, any outer-table row that never matched gets emitted once, paired with NULLs. For a hash join doing a LEFT JOIN, the *probe* side is the left table, and unmatched probe rows are tracked and flushed with NULLs at the end.

**Why join order matters**: for a chain of joins (A ⋈ B ⋈ C), the optimizer picks an order that minimizes the size of intermediate result sets. This is why filtering (`WHERE`) as early as possible, and having good indexes/statistics, dramatically affects performance — the optimizer's cost estimates are only as good as its statistics.

**Correlated subquery internal working**: unlike a join, a naively-executed correlated subquery re-runs the inner query once *per row* of the outer query (O(N × M)). Modern optimizers often **rewrite** correlated subqueries into semi-joins or anti-joins (for `EXISTS`/`NOT EXISTS`/`IN`/`NOT IN`) internally so they don't actually pay that cost — this is called "subquery unnesting" or "decorrelation."

---

### 1.3 Real-World Examples (💼)

1. **E-commerce order dashboard**: `INNER JOIN` between `orders` and `customers` to show only orders that belong to a known customer; a `LEFT JOIN` from `products` to `order_items` to report which products have **never been ordered** (`WHERE order_items.id IS NULL`) — a classic "find the gap" LEFT JOIN pattern used for slow-moving inventory reports.

2. **Attendance / HR systems**: `LEFT JOIN` from `employees` to `attendance_logs` for a given date, so employees with **no punch-in record** still show up in the report (as absent), instead of silently disappearing like an INNER JOIN would cause.

3. **Analytics / reconciliation**: `FULL JOIN` between two systems' transaction tables (e.g., internal ledger vs. payment gateway records) to find mismatches in both directions — transactions present in one system but missing in the other.

4. **Recommendation systems**: correlated subqueries or window functions to find, per category, the top-N selling products — though in modern SQL this is usually replaced by window functions (`RANK() OVER (PARTITION BY ...)`), the subquery version is still commonly asked as a precursor concept.

5. **Fraud / audit checks**: `EXISTS` subqueries to flag accounts that have at least one transaction over a threshold, without needing to pull the transaction rows themselves — much cheaper than a join when you only care about existence.

---

### 1.4 Complete Code Example — Runnable End-to-End (💻)

```sql
-- ===== SCHEMA =====
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
    dept_id   INT PRIMARY KEY,
    dept_name VARCHAR(50) NOT NULL
);

CREATE TABLE employee (
    emp_id    INT PRIMARY KEY,
    emp_name  VARCHAR(50) NOT NULL,
    dept_id   INT,
    salary    DECIMAL(10,2),
    manager_id INT,
    CONSTRAINT fk_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id),
    CONSTRAINT fk_mgr FOREIGN KEY (manager_id) REFERENCES employee(emp_id)
);

-- ===== DATA =====
INSERT INTO department VALUES
(1,'Engineering'), (2,'Sales'), (3,'HR'), (4,'Marketing');

INSERT INTO employee (emp_id, emp_name, dept_id, salary, manager_id) VALUES
(101,'Alice',1,90000,NULL),
(102,'Bob',1,85000,101),
(103,'Carol',2,70000,NULL),
(104,'Dan',NULL,60000,103),
(105,'Eve',3,75000,NULL);

-- ===== QUERIES =====

-- Q1: Every employee with their department (INNER)
SELECT e.emp_name, d.dept_name
FROM employee e
INNER JOIN department d ON e.dept_id = d.dept_id;

-- Q2: Every employee, department NULL if none (LEFT)
SELECT e.emp_name, d.dept_name
FROM employee e
LEFT JOIN department d ON e.dept_id = d.dept_id;

-- Q3: Departments with no employees (LEFT + anti-join pattern)
SELECT d.dept_name
FROM department d
LEFT JOIN employee e ON e.dept_id = d.dept_id
WHERE e.emp_id IS NULL;

-- Q4: Self-join — employees earning more than their manager
SELECT emp.emp_name AS employee, mgr.emp_name AS manager
FROM employee emp
JOIN employee mgr ON emp.manager_id = mgr.emp_id
WHERE emp.salary > mgr.salary;

-- Q5: Correlated subquery — employees earning above their department's average
SELECT e.emp_name, e.salary
FROM employee e
WHERE e.salary > (
    SELECT AVG(e2.salary) FROM employee e2 WHERE e2.dept_id = e.dept_id
);

-- Q6: EXISTS — departments that have at least one employee earning > 80000
SELECT d.dept_name
FROM department d
WHERE EXISTS (
    SELECT 1 FROM employee e WHERE e.dept_id = d.dept_id AND e.salary > 80000
);
```

---

## PART 2: HIBERNATE

### 2.1 Interview-Ready Notes

**What is Hibernate?** An Object-Relational Mapping (ORM) framework for Java. It maps Java classes (POJOs/entities) to database tables, so you work with objects instead of writing raw JDBC/SQL for CRUD. It implements the JPA (Jakarta Persistence API) specification — JPA is the spec/interface, Hibernate is (the most popular) implementation.

#### Architecture

Core building blocks, from configuration to database:

1. **Configuration** — reads `hibernate.cfg.xml` / `application.properties` (DB URL, dialect, credentials, mapping info).
2. **SessionFactory** — built once per application (heavyweight, thread-safe). Caches compiled mappings and provides `Session` objects. In Spring Boot this is created for you automatically.
3. **Session** — lightweight, **not thread-safe**, represents a single unit of work / conversation with the database. Wraps a JDBC connection. Provides `save()`, `get()`, `update()`, `delete()`, `createQuery()`, etc. Maintains the **first-level cache** (persistence context).
4. **Transaction** — wraps a unit of work in atomicity; `session.beginTransaction()` / `commit()` / `rollback()`.
5. **Query / Criteria API / HQL** — ways to query: HQL (Hibernate Query Language, object-oriented SQL), Criteria API (programmatic, type-safe), or native SQL.
6. **Persistent objects (Entities)** — POJOs annotated with `@Entity`, mapped to tables, tracked by Hibernate through their lifecycle.

**Entity lifecycle states:**
- **Transient** — a plain `new` object, not associated with any Session, not in the DB.
- **Persistent (managed)** — attached to an open Session; Hibernate tracks changes to it (dirty checking) and will sync them to the DB.
- **Detached** — was persistent, but its Session has closed. Changes are no longer tracked automatically; must `merge()` it back in to resume tracking.
- **Removed** — marked for deletion within a transaction.

#### Entity

```java
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empId;

    @Column(name = "emp_name", nullable = false, length = 50)
    private String empName;

    private BigDecimal salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    // getters, setters, no-arg constructor (required by Hibernate)
}
```
Key annotations: `@Entity` (marks a persistent class), `@Table` (maps to a specific table name if different from the class name), `@Id` (primary key), `@GeneratedValue` (auto-increment strategy: `IDENTITY`, `SEQUENCE`, `TABLE`, `AUTO`), `@Column` (column-level customization).

#### Repository (Spring Data JPA layer, built on top of Hibernate)

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment_DeptId(Long deptId);
    List<Employee> findBySalaryGreaterThan(BigDecimal salary);

    @Query("SELECT e FROM Employee e WHERE e.department.deptName = :name")
    List<Employee> findByDeptName(@Param("name") String name);
}
```
Spring Data JPA generates the implementation at runtime from method names (**query derivation**) or from `@Query` (HQL/JPQL or native SQL with `nativeQuery = true`). Under the hood it still goes through Hibernate's `Session`/`EntityManager`.

#### CRUD

```java
// CREATE
Session session = sessionFactory.openSession();
session.beginTransaction();
Employee e = new Employee();
e.setEmpName("Frank");
e.setSalary(new BigDecimal("72000"));
session.save(e);           // or session.persist(e)
session.getTransaction().commit();

// READ
Employee found = session.get(Employee.class, 101L);   // hits DB (or 1st-level cache) immediately
Employee ref    = session.load(Employee.class, 101L);  // returns a lazy proxy, hits DB only on field access

// UPDATE (dirty checking — no explicit save() call needed inside an open transaction)
session.beginTransaction();
Employee e2 = session.get(Employee.class, 101L);
e2.setSalary(new BigDecimal("95000"));
session.getTransaction().commit();   // Hibernate detects the change and issues an UPDATE automatically

// DELETE
session.beginTransaction();
Employee e3 = session.get(Employee.class, 104L);
session.delete(e3);
session.getTransaction().commit();
```

With Spring Data JPA, this collapses to `repository.save(e)`, `repository.findById(id)`, `repository.deleteById(id)`.

#### Relationship Mappings

**@OneToOne** — one row in A maps to exactly one row in B (e.g., `Employee` ↔ `EmployeeProfile`).
```java
@Entity
public class Employee {
    @Id private Long empId;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private EmployeeProfile profile;
}

@Entity
public class EmployeeProfile {
    @Id private Long profileId;

    @OneToOne
    @JoinColumn(name = "emp_id")   // owning side — holds the FK
    private Employee employee;
}
```

**@OneToMany / @ManyToOne** — the classic parent-child relationship (e.g., one `Department` has many `Employee`s).
```java
@Entity
public class Department {
    @Id private Long deptId;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees = new ArrayList<>();
}

@Entity
public class Employee {
    @Id private Long empId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")   // this side owns the FK column
    private Department department;
}
```
Rule of thumb: **`@ManyToOne` is (almost) always the owning side** because that's the table that physically holds the foreign key column. The `@OneToMany` side uses `mappedBy` to say "I'm the inverse side, don't manage the FK, just mirror it."

**@ManyToMany** (bonus, commonly follow-up asked): needs a join table.
```java
@Entity
public class Employee {
    @ManyToMany
    @JoinTable(name = "employee_project",
        joinColumns = @JoinColumn(name = "emp_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id"))
    private List<Project> projects;
}
```

#### Lazy vs Eager

| | LAZY | EAGER |
|---|---|---|
| When loaded | On first access to the field (via a proxy) | Immediately, along with the parent, usually via a JOIN |
| Default for | `@OneToMany`, `@ManyToMany` | `@ManyToOne`, `@OneToOne` |
| Risk | `LazyInitializationException` if accessed after the Session closes | N+1 query problem or over-fetching if used carelessly |
| Typical fix | Fetch join in the query, or `@EntityGraph`, or restructure to fetch within the transaction | Explicitly override with `fetch = FetchType.LAZY` where eager isn't needed |

```java
@ManyToOne(fetch = FetchType.LAZY)   // override the default EAGER
@JoinColumn(name = "dept_id")
private Department department;
```

**The N+1 problem**: fetching N parent rows, then lazily triggering N additional queries (one per row) to fetch each one's children — instead of a single JOIN. Classic Hibernate interview trap. Fixed with:
```java
// JPQL fetch join — pulls employees eagerly in ONE query
@Query("SELECT d FROM Department d JOIN FETCH d.employees WHERE d.deptId = :id")
Department findWithEmployees(@Param("id") Long id);
```

---

### 2.2 Internal Working (🧠)

**SessionFactory creation**: on startup, Hibernate parses all `@Entity` classes (or XML mappings) via reflection, builds an internal metamodel, validates mappings against the configured `Dialect` (e.g., `MySQLDialect`, `PostgreSQLDialect` — this is what lets Hibernate generate DB-specific SQL), and creates a connection pool. This is expensive, which is why `SessionFactory` is built exactly once and shared app-wide.

**First-level cache (Session/Persistence Context)**: every `Session` keeps a map of `entityId → entity instance` for everything it has loaded or saved in the current unit of work. Calling `session.get(Employee.class, 101L)` twice in the same session hits the DB only once — the second call is served from this cache. This cache is mandatory and can't be disabled; it's scoped to a single Session (so it disappears once the Session closes).

**Second-level cache** (optional, shared across Sessions, app-wide — e.g., Ehcache, Redis): sits between the Session and the database. Must be explicitly enabled per-entity with `@Cacheable`. Useful for read-heavy, rarely-changing reference data (e.g., `Department`, country lists).

**Dirty checking**: when a transaction commits (or the Session flushes), Hibernate compares each managed entity's current field values against a snapshot taken when it was first loaded. Any differences generate `UPDATE` statements automatically — this is why you don't call `update()` explicitly for entities you fetched with `get()` inside an open transaction.

**Flush**: the act of synchronizing the in-memory Session state to the database (executing the pending INSERT/UPDATE/DELETE SQL). By default this happens automatically before a transaction commits, and also before executing a query that could be affected by pending changes (`FlushMode.AUTO`). Flushing is *not* the same as committing — a flush sends SQL to the DB, but doesn't end the transaction; a rollback after a flush still undoes it.

**Lazy loading internals**: for a `LAZY` association, Hibernate doesn't put the real entity/collection in the field — it injects a **runtime-generated proxy** (via bytecode enhancement/CGLIB or a `PersistentBag`/`PersistentSet` wrapper for collections). The proxy holds just the ID. The first time you call a method on it (other than `getId()`), it triggers a fresh `SELECT` against the still-open Session. If the Session has already closed, this throws `LazyInitializationException` — one of the most commonly hit real-world Hibernate bugs.

**HQL → SQL translation**: HQL is parsed into an internal AST, resolved against the entity metamodel (so `Employee.department.deptName` becomes a real join+column), and translated into dialect-specific SQL, which is then executed via JDBC exactly like hand-written SQL — Hibernate still ultimately runs plain SQL under the hood, it just generates it for you.

**Transaction + connection**: a `Session` wraps one JDBC `Connection`. `beginTransaction()` disables autocommit and starts a DB transaction; `commit()` flushes, commits the JDBC transaction, and (in most configurations) closes/releases the connection back to the pool.

---

### 2.3 Real-World Examples (💼)

1. **N+1 in production**: a REST endpoint `/departments` that serializes each department's employee list triggers 1 query for departments + N queries for each department's employees under LAZY — a well-known real incident pattern fixed with `JOIN FETCH` or `@EntityGraph`, or by using a DTO projection query instead of serializing the whole entity graph.

2. **LazyInitializationException in a web app**: a Spring MVC controller loads an `Employee` in the service layer (Session closes when the `@Transactional` method returns), then the view/serializer tries to access `employee.getDepartment().getDeptName()` outside that transaction → exception. Fixed by either eager-fetching what the view needs, using `@Transactional` around the whole request (open-session-in-view — generally discouraged for performance reasons), or mapping to a DTO inside the transactional boundary.

3. **Cascade misconfiguration causing data loss**: `cascade = CascadeType.ALL` with `orphanRemoval = true` on a `@OneToMany` means removing a child from the parent's collection issues a `DELETE` — teams have accidentally wiped child records by simply reassigning a list reference instead of mutating it in place.

4. **Batch inserts**: inserting 10,000 rows one at a time via `save()` in a loop is slow because each triggers its own round trip; real systems configure `hibernate.jdbc.batch_size` and periodically call `session.flush(); session.clear();` to batch INSERTs and avoid the first-level cache growing unbounded (a memory leak pattern in long-running batch jobs).

5. **Second-level cache for reference/lookup tables**: an e-commerce catalog caches `Category` and `Country` entities with Ehcache/Redis as L2 cache since they change rarely but are read on nearly every request — cutting DB load significantly.

---

### 2.4 Complete Code Example — Runnable End-to-End (💻)

```java
// ===== ENTITIES =====

@Entity
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deptId;

    @Column(name = "dept_name", nullable = false)
    private String deptName;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees = new ArrayList<>();

    public Department() {}
    public Department(String deptName) { this.deptName = deptName; }

    // helper to keep both sides of the relationship in sync
    public void addEmployee(Employee e) {
        employees.add(e);
        e.setDepartment(this);
    }

    // getters/setters omitted for brevity
}

@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empId;

    @Column(name = "emp_name", nullable = false)
    private String empName;

    private BigDecimal salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    public Employee() {}
    public Employee(String empName, BigDecimal salary) {
        this.empName = empName;
        this.salary = salary;
    }

    // getters/setters omitted for brevity
    public void setDepartment(Department d) { this.department = d; }
}

// ===== REPOSITORY (Spring Data JPA) =====

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartment_DeptId(Long deptId);

    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.salary > :min")
    List<Employee> findHighEarnersWithDept(@Param("min") BigDecimal min);
}

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.deptId = :id")
    Optional<Department> findWithEmployees(@Param("id") Long id);
}

// ===== SERVICE LAYER =====

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepo;
    private final DepartmentRepository deptRepo;

    public EmployeeService(EmployeeRepository employeeRepo, DepartmentRepository deptRepo) {
        this.employeeRepo = employeeRepo;
        this.deptRepo = deptRepo;
    }

    @Transactional
    public Employee hireEmployee(String name, BigDecimal salary, Long deptId) {
        Department dept = deptRepo.findById(deptId)
            .orElseThrow(() -> new EntityNotFoundException("Department not found: " + deptId));
        Employee emp = new Employee(name, salary);
        dept.addEmployee(emp);          // keeps both sides consistent
        return employeeRepo.save(emp);  // cascades are not needed here since Employee is the owning side
    }

    @Transactional
    public void giveRaise(Long empId, BigDecimal newSalary) {
        Employee emp = employeeRepo.findById(empId)
            .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + empId));
        emp.setSalary(newSalary);
        // no explicit save() call needed — dirty checking issues the UPDATE on commit
    }

    @Transactional(readOnly = true)
    public List<Employee> highEarners(BigDecimal threshold) {
        return employeeRepo.findHighEarnersWithDept(threshold); // avoids N+1 via JOIN FETCH
    }
}
```

---

## PART 3: INTERVIEW QUESTIONS (❓ 35 Q&A)

### SQL Joins

**1. What's the difference between INNER JOIN and LEFT JOIN?**
INNER JOIN returns only matching rows from both tables. LEFT JOIN returns all rows from the left table regardless of a match, filling unmatched right-side columns with NULL.

**2. Why doesn't MySQL support FULL JOIN?**
It's a historical implementation choice; you emulate it with `LEFT JOIN UNION RIGHT JOIN` (using `UNION`, not `UNION ALL`, to drop the duplicated matched rows — or `UNION ALL` plus a `WHERE` filter if you want to control duplicates explicitly).

**3. What happens if you JOIN without an ON clause?**
Without `ON`/`USING`, most engines either error (ANSI join syntax) or, with old comma-join syntax, produce a CROSS JOIN (Cartesian product) — every row of A with every row of B.

**4. What's a self-join and when would you use one?**
A table joined to itself via aliases, e.g., finding each employee's manager where both live in the same `employee` table.

**5. Explain the difference between WHERE and HAVING in a query with a JOIN and GROUP BY.**
`WHERE` filters rows before grouping/aggregation; `HAVING` filters groups after aggregation (e.g., `HAVING COUNT(*) > 5`).

**6. What is a Cartesian product and how do joins avoid it?**
The full pairwise combination of two tables' rows. A proper `ON` condition restricts the join to only matching rows, avoiding an unintentional Cartesian product (a common bug from a missing/incorrect join condition).

**7. Difference between UNION and UNION ALL?**
`UNION` deduplicates the combined result set (requires a sort/hash for dedup, costlier); `UNION ALL` keeps all rows including duplicates and is faster.

**8. What is a correlated subquery, and why can it be slow?**
A subquery that references a column from the outer query, conceptually re-executed once per outer row — O(N×M) if not optimized by the query planner into a join/semi-join.

**9. IN vs EXISTS — when do you prefer one over the other?**
`EXISTS` short-circuits on the first match and is generally preferred for large subqueries or when the subquery might return NULLs (which can make `NOT IN` behave unexpectedly). `IN` is fine and readable for small, static lists.

**10. Why can `NOT IN` silently return zero rows when the subquery contains a NULL?**
Because `x NOT IN (1, NULL)` evaluates to `UNKNOWN` (not `TRUE`) for every row — SQL's three-valued logic. Use `NOT EXISTS` instead, or filter out NULLs explicitly, to avoid this trap.

**11. What's the difference between a subquery in the WHERE clause and one in the FROM clause?**
A `WHERE`-clause subquery filters rows (scalar or list); a `FROM`-clause subquery (derived table) is itself queried like a virtual table, letting you join/aggregate on its result.

**12. How would you find duplicate rows using a self-join or GROUP BY?**
```sql
SELECT emp_name, COUNT(*) FROM employee GROUP BY emp_name HAVING COUNT(*) > 1;
```

**13. What is a hash join and when does the optimizer choose it?**
An algorithm that builds an in-memory hash table on the smaller table's join key, then probes it with the larger table. Chosen for large, unindexed equi-joins.

**14. What indexes help a JOIN perform well?**
An index on the join column(s) of at least the inner/probed table, ideally both sides; composite indexes when joining/filtering on multiple columns together.

**15. What's the difference between a LEFT JOIN with a WHERE filter on the right table vs. the same filter in the ON clause?**
Putting the filter in `ON` keeps it part of the outer join logic (still returns all left rows, filtering only which right rows match). Putting it in `WHERE` runs *after* the join, so it can turn unmatched (NULL) rows into filtered-out rows, effectively converting the LEFT JOIN into an INNER JOIN for that condition.

### Hibernate

**16. What is the difference between `session.get()` and `session.load()`?**
`get()` hits the database (or 1st-level cache) immediately and returns `null` if not found. `load()` returns a lazy proxy without hitting the DB until a field is accessed, and throws `ObjectNotFoundException` on access if the row doesn't exist.

**17. What is the first-level cache?**
The mandatory, per-`Session` cache of loaded/saved entities keyed by ID; guarantees that within one Session, repeated loads of the same ID return the same object instance without a duplicate DB hit.

**18. What is dirty checking?**
Hibernate's mechanism of comparing an entity's current state to its loaded snapshot at flush/commit time, automatically generating `UPDATE` statements for changed fields — no explicit `update()` call needed for managed entities.

**19. Explain the N+1 select problem and two ways to fix it.**
Fetching a list of parents (1 query), then lazily loading each parent's children individually (N queries). Fixed via `JOIN FETCH` in JPQL, `@EntityGraph`, or batch fetching (`@BatchSize`/`hibernate.default_batch_fetch_size`).

**20. What's the difference between `@OneToMany(mappedBy=...)` and the owning side of a relationship?**
The owning side holds the foreign key column and is where Hibernate looks to persist the relationship; the inverse (`mappedBy`) side just mirrors it for convenience and isn't itself responsible for the FK.

**21. What does `CascadeType.ALL` do, and what's a risk with it?**
Propagates all persistence operations (persist, merge, remove, refresh, detach) from parent to child. Risk: combined with `orphanRemoval = true`, removing a child from a collection can silently delete it from the DB.

**22. What is a `LazyInitializationException` and how do you avoid it?**
Thrown when code tries to access a lazily-loaded, uninitialized association after its owning Session has closed. Avoided by fetching what's needed inside the transaction, using `JOIN FETCH`/`@EntityGraph`, or mapping to DTOs before the Session closes.

**23. What's the difference between `merge()` and `update()`?**
`update()` reattaches a detached entity assuming no other managed copy with the same ID exists in the Session (throws if it does); `merge()` copies the detached entity's state onto a managed instance (loading it if necessary) and returns that managed instance — generally the safer, more commonly used option.

**24. What is the Hibernate Session's role vs. the EntityManager (JPA)?**
`EntityManager` is the JPA-standard interface for a persistence context; Hibernate's `Session` extends it with Hibernate-specific extras. In a Spring Data JPA app, you typically interact with repositories, which use `EntityManager` under the hood (backed by Hibernate's `Session`).

**25. What is `@Transactional` doing under the hood in a Spring + Hibernate app?**
It wraps the annotated method in an AOP proxy that begins a transaction (and opens/binds a Session) before the method runs, and commits (or rolls back on an unchecked exception) after it returns.

**26. Difference between FetchType.LAZY and EAGER, and their defaults?**
LAZY defers loading until access (default for `@OneToMany`/`@ManyToMany`); EAGER loads immediately with the parent (default for `@ManyToOne`/`@OneToOne`).

**27. What is HQL and how does it differ from native SQL?**
Hibernate Query Language — object-oriented, operates on entity names/fields rather than table/column names, and is translated to dialect-specific SQL by Hibernate. Native SQL bypasses this and is written directly against the schema.

**28. What is the second-level cache, and when would you enable it?**
An optional cache shared across Sessions (e.g., Ehcache, Redis), used for read-heavy, infrequently-changing entities like reference/lookup data, enabled per-entity via `@Cacheable`.

**29. How does Hibernate handle optimistic locking?**
Via a `@Version` column; on update, Hibernate includes `WHERE version = <loaded version>` in the SQL, and if 0 rows are affected (someone else updated it first), it throws `OptimisticLockException`.

**30. What's the difference between `persist()` and `save()`?**
`persist()` (JPA-standard) has no return value and guarantees the INSERT is delayed until flush; `save()` (Hibernate-specific) returns the generated ID immediately and may execute the INSERT right away.

**31. What is an entity's lifecycle in Hibernate (transient, persistent, detached, removed)?**
See Section 2.1 — transient (new, unmanaged) → persistent (managed, tracked in a Session) → detached (was managed, Session closed) → removed (marked for deletion in a transaction).

**32. How would you paginate results efficiently with Hibernate/JPA?**
`Pageable`/`setFirstResult()`+`setMaxResults()` for offset pagination (simple but slow on large offsets); keyset/cursor-based pagination (`WHERE id > :lastSeenId ORDER BY id LIMIT n`) for better performance on large datasets.

**33. What is a `@JoinColumn` vs `@JoinTable`?**
`@JoinColumn` specifies the FK column for a direct relationship (`@ManyToOne`, `@OneToOne`); `@JoinTable` defines an intermediate join table, needed for `@ManyToMany` (or unidirectional `@OneToMany` without a FK on the child).

**34. How do batch inserts/updates work in Hibernate, and what config enables them?**
`hibernate.jdbc.batch_size` groups multiple INSERT/UPDATE statements into fewer JDBC round trips; requires periodic `flush()`/`clear()` in a loop to avoid the first-level cache growing unbounded during large batch jobs.

**35. What's the difference between `CascadeType.REMOVE` and `orphanRemoval = true`?**
`CascadeType.REMOVE` deletes children when the parent itself is explicitly deleted. `orphanRemoval = true` additionally deletes a child the moment it's *removed from the parent's collection*, even if the parent isn't deleted.

---

## PART 4: CODING PROBLEMS (🧩)

### Problem 1 — Second Highest Salary (SQL)
**Given** the `employee` table, write a query to find the second-highest salary. Return `NULL` if it doesn't exist.

```sql
SELECT MAX(salary) AS second_highest
FROM employee
WHERE salary < (SELECT MAX(salary) FROM employee);
```
*Alternative with `DENSE_RANK()` (generalizes to Nth highest):*
```sql
SELECT salary FROM (
    SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
    FROM employee
) ranked
WHERE rnk = 2;
```

### Problem 2 — Departments With More Than 1 Employee
```sql
SELECT d.dept_name, COUNT(e.emp_id) AS emp_count
FROM department d
JOIN employee e ON e.dept_id = d.dept_id
GROUP BY d.dept_name
HAVING COUNT(e.emp_id) > 1;
```

### Problem 3 — Employees Earning More Than Their Manager (Self-Join)
```sql
SELECT emp.emp_name
FROM employee emp
JOIN employee mgr ON emp.manager_id = mgr.emp_id
WHERE emp.salary > mgr.salary;
```

### Problem 4 — Departments With Zero Employees (Anti-Join Pattern)
```sql
SELECT d.dept_name
FROM department d
LEFT JOIN employee e ON d.dept_id = e.dept_id
WHERE e.emp_id IS NULL;
```

### Problem 5 — Find Duplicate Emails (classic LeetCode-style)
```sql
SELECT email, COUNT(*) AS cnt
FROM person
GROUP BY email
HAVING COUNT(*) > 1;
```

### Problem 6 (Java/Hibernate) — Fix the N+1 Bug
**Given** this buggy code that triggers N+1 queries when listing departments and their employee counts, rewrite it to run in a single query.

```java
// BUGGY — triggers 1 query for departments + N queries for employees
List<Department> depts = departmentRepository.findAll();
for (Department d : depts) {
    System.out.println(d.getDeptName() + ": " + d.getEmployees().size());
}
```
**Fix — projection query, no entity graph traversal needed:**
```java
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query("SELECT d.deptName, COUNT(e) FROM Department d LEFT JOIN d.employees e GROUP BY d.deptName")
    List<Object[]> findDeptEmployeeCounts();
}
```

### Problem 7 (Java/Hibernate) — Implement Optimistic Locking
Add a version column to prevent lost updates on concurrent salary edits:
```java
@Entity
public class Employee {
    @Id private Long empId;
    private BigDecimal salary;

    @Version
    private Integer version;   // Hibernate auto-manages this
}
```
Concurrent updates to the same row now throw `OptimisticLockException` on the losing transaction, instead of silently overwriting.

### Problem 8 — Write a Query to Detect the N+1 Pattern's Fix Working
Using `JOIN FETCH` to confirm employees load in a department's single query:
```sql
-- What Hibernate generates for a JOIN FETCH JPQL query:
SELECT d.*, e.*
FROM department d
LEFT JOIN employee e ON e.dept_id = d.dept_id
WHERE d.dept_id = ?;
```

---

## PART 5: AI-STYLE INTERVIEW QUESTIONS (🤖)

These are the "reasoning-under-ambiguity" style questions increasingly used by AI/ML and senior backend interviewers — they test judgment, not memorized syntax.

**1. "Your LEFT JOIN query is returning fewer rows than expected from the left table. Walk me through how you'd debug it."**
Expected reasoning: check whether a filter on the right table's column got placed in `WHERE` instead of `ON` (silently converting it into an INNER JOIN); check for a many-to-many fan-out elsewhere in the query inflating row counts and then getting filtered by an aggregate; verify the join key data types/NULL handling match.

**2. "A teammate proposes replacing all your codebase's JOINs with subqueries for 'better performance.' How do you respond?"**
Expected reasoning: performance depends on the optimizer, indexes, and data volume, not the syntax itself — modern optimizers often produce the same execution plan either way. Push back with data (`EXPLAIN ANALYZE`) rather than agreeing or disagreeing on principle; note joins are usually clearer when you need columns from both tables, while `EXISTS` subqueries are often clearer/faster for pure existence checks.

**3. "How would you design the fetch strategy for an API that returns a Department with all its Employees, at scale?"**
Expected reasoning: default LAZY avoids over-fetching for endpoints that don't need employees; use a targeted `JOIN FETCH` or `@EntityGraph` for the specific endpoint that does; consider pagination on the employee list itself if departments can be very large, and DTO projections to avoid serializing the whole entity graph (and avoiding accidental lazy-loading during JSON serialization).

**4. "You're seeing intermittent `OptimisticLockException`s in production. Is this a bug?"**
Expected reasoning: not necessarily — it may be the concurrency-control mechanism working as intended under genuine concurrent writes. The real question is whether the *retry strategy* for the losing transaction is correct (e.g., re-fetch and reapply the business logic) versus surfacing a raw 500 to the user.

**5. "Explain to a non-technical product manager why 'just add more joins' isn't free, using an analogy."**
Expected reasoning: tests communication, not correctness — e.g., "each join is like cross-referencing two spreadsheets by hand; the bigger the spreadsheets and the more of them, the longer it takes, unless you have a good index (like a sorted phone book) to jump straight to the row you need."

**6. "Given a slow query with three joins, what's your systematic approach, not just 'add an index'?"**
Expected reasoning: run `EXPLAIN ANALYZE` first to see the actual plan and where time is spent; check join order and whether statistics are stale; check for missing indexes on join/filter columns; check if a wide `SELECT *` is pulling unnecessary columns/joins that could be trimmed; consider whether the query needs to be a single monolithic join at all or could be decomposed.

**7. "How would you decide between LAZY and EAGER for a new association, if you didn't know all the future call sites yet?"**
Expected reasoning: default to LAZY (safer default, avoids over-fetching and N+1 blowups by default) and opt into eager loading per-query with `JOIN FETCH`/`@EntityGraph` where a specific use case needs it — rather than baking a global EAGER default that every future caller pays for.

**8. "If an AI code-generation tool suggested `session.load()` where you expected `null`-checking behavior, what would you flag?"**
Expected reasoning: `load()` never returns `null` — it returns a proxy and defers the "not found" failure to first field access, throwing `ObjectNotFoundException` there instead. Code that does `if (session.load(...) == null)` is a bug; `get()` is the right call when you need to check existence.

---

## PART 6: MOCK INTERVIEW (🎤)

*A realistic back-and-forth transcript — useful for rehearsing pacing and follow-up depth.*

**Interviewer:** Let's start simple. What's the difference between an INNER JOIN and a LEFT JOIN?

**You:** An INNER JOIN only returns rows where the join condition matches on both sides — anything unmatched on either table is dropped. A LEFT JOIN keeps every row from the left table no matter what, and just fills in NULLs for the right table's columns when there's no match.

**Interviewer:** Good. Can you give me a real scenario where LEFT JOIN matters and INNER JOIN would give you a wrong answer?

**You:** Sure — say I want to report on which products have never been ordered, for an inventory review. If I INNER JOIN `products` to `order_items`, products with zero orders just vanish from the result, which is the opposite of what I want. A LEFT JOIN from `products` to `order_items`, then filtering `WHERE order_items.id IS NULL`, gives me exactly the unordered products.

**Interviewer:** Nice. Now, suppose that LEFT JOIN query is returning way fewer rows than you expect — fewer products than actually exist. What would you check?

**You:** First thing I'd check is whether there's a filter on the right table sitting in the WHERE clause instead of the ON clause. If I wrote `WHERE order_items.status = 'shipped'`, that silently turns my LEFT JOIN back into something that behaves like an INNER JOIN, because unmatched rows have NULL status, and NULL never satisfies `= 'shipped'`. I'd move that condition into the ON clause if I still want unmatched left rows to survive.

**Interviewer:** Exactly the trap I was fishing for. Let's switch to Hibernate. Tell me about the N+1 problem.

**You:** It happens when you fetch a list of parent entities — say, N departments — and then, for each one, lazily access a LAZY collection like its employees. Instead of one query with a join, you get 1 query for the departments plus N more queries, one per department, to fetch each one's employees. It's easy to miss in development with small datasets and painful in production with real data volumes.

**Interviewer:** How do you fix it?

**You:** A few options depending on the situation. If I know a specific use case always needs the employees too, I'll use a `JOIN FETCH` in the JPQL query, or an `@EntityGraph`, to pull both in a single query. If it's more about counts or aggregates, I'd just write a projection query — like `COUNT(e)` grouped by department — rather than loading full entity graphs at all. I try not to just flip the mapping to EAGER globally, because that penalizes every other caller that didn't need the employees.

**Interviewer:** Good instinct. One more — what's a `LazyInitializationException`, and when have you actually hit one?

**You:** It happens when you try to access a lazily-loaded association after its Session has already closed — so there's no active persistence context left to run the query that would populate it. A common real case is a service method loading an entity inside a `@Transactional` boundary, returning it, and then a serializer or view layer touching a lazy field afterward, once the transaction (and Session) has ended. The fix is usually to either fetch what you need while still inside the transaction — with a fetch join or explicit access — or map to a DTO before the boundary closes, rather than passing the raw entity out and hoping nothing lazy gets touched later.

**Interviewer:** That's a solid answer. Last one, and it's a bit open-ended: if I told you to replace every JOIN FETCH in a codebase with EAGER associations to "simplify things," what would you say?

**You:** I'd push back, respectfully. EAGER isn't actually simpler — it just moves the cost to every single query that touches that entity, whether or not that particular call site needs the association. You'd very likely trade an occasional, fixable N+1 for a permanent over-fetching tax on every read, plus a higher risk of accidentally pulling in large, unbounded collections you didn't intend to load. I'd rather keep LAZY as the default and be deliberate about eager-loading per query, where it's actually needed.

**Interviewer:** Great, that's exactly the kind of judgment I wanted to hear. Thanks — that's all the questions I have.

---

*End of guide.*
