# Questions

---

## Java & Object-Oriented Design

**1.** The `Auditable` abstract class in this codebase uses `@MappedSuperclass`. Explain
what this annotation tells JPA, and describe what would happen to the database schema if
you removed it. Why is it better to put `createdAt` and `updatedAt` in a shared abstract
class rather than adding those fields directly to `Transaction` and `Account` separately?

`@MappedSuperclass` tells JPA: "don't create a table for this class, but do inherit its
fields into every subclass's table." So `transactions` gets `created_at` and `updated_at`
columns directly, and so does `accounts` — no join, no separate table. If you removed the
annotation, JPA would ignore the superclass entirely and those columns would disappear from
both tables silently. The schema would change without any error at startup.

The reason to centralise these fields in `Auditable` rather than copy them into each entity
is maintainability. If you add a third entity later, it extends `Auditable` and gets the
timestamps for free. If you ever need to change the behaviour — say, switching from
`LocalDateTime` to `ZonedDateTime` — you change it in one place instead of hunting down
every entity that copy-pasted the same two fields. Copy-paste inheritance is how bugs get
introduced: one entity gets updated, the other doesn't.

---

**2.** `TransactionService` is defined as a Java interface, with `TransactionServiceImpl`
as its only implementation. A new engineer on the team asks: "Why bother with the interface
if there's only one implementation? Isn't it just extra boilerplate?" How do you respond?
Give at least one concrete scenario where the interface pays off.

The interface pays off immediately in testing. `TransactionController` depends on
`TransactionService`, not on `TransactionServiceImpl`. That means in a `@WebMvcTest` you
can inject a `@MockBean` of the interface and test the controller in complete isolation —
no database, no Spring context overhead. If the controller depended on the concrete class,
mocking it would be harder and more fragile.

Beyond testing, the interface makes the contract explicit. It says: "here are the operations
this layer exposes, and here is nothing else." A developer reading the codebase can
understand what the service does by reading the interface alone, without wading through
implementation details. If you ever need a second implementation — say, a caching wrapper,
or a read-only implementation for a reporting service — the interface is already there.
"We only have one implementation right now" is not the same as "we will only ever have one
implementation."

---

**3.** `Category` is modelled as an enum rather than a plain `String` field on
`Transaction`. What does storing it as `@Enumerated(EnumType.STRING)` in the database
actually produce in the table? What would go wrong if a future developer added a new
category value to the enum but forgot to handle database migration?

`@Enumerated(EnumType.STRING)` stores the enum's name as a plain VARCHAR in the database.
So the `category` column contains values like `"FOOD"`, `"TRANSPORT"`, `"UTILITIES"` — the
exact string you'd get from `Category.FOOD.name()`. The alternative, `EnumType.ORDINAL`,
stores the integer position (0, 1, 2...) which is a trap: insert a new enum value in the
middle and every existing row silently points to the wrong category.

If a developer adds a new value like `HEALTHCARE` to the enum but doesn't add it to the
database schema's CHECK constraint (if one exists), the application will write it fine but
any tooling or database-level constraint validation will reject it. More dangerously, if
the column has a NOT NULL constraint and the new value doesn't match existing data during
a migration, the migration will fail. In either case the issue doesn't show up in unit tests
because the tests use H2 in-memory with no constraints — it only blows up in production.
This is why enum values in a database should always be treated as a migration concern, not
just a code change.

---

**4.** `BudgetCalculator` is a `final` class with a private constructor and a single
static method. What pattern is this, and why is it appropriate for this specific utility?
In your implementation, what data structure did you use as an intermediate step before
building the final sorted map, and why?

This is the utility class pattern — sometimes called a static helper or pure function
class. Making it `final` prevents subclassing (there's no reason to extend a stateless
helper). The private constructor prevents instantiation. You're communicating to every
reader: "this class holds stateless logic, you call it, you don't instantiate it." It's
appropriate here because `getTopSpendingCategories` has no state — it takes input, does
computation, returns output. Wrapping it in an instantiable class would add ceremony with
no benefit.

As an intermediate step I used a regular `HashMap` from `Collectors.groupingBy` to sum
amounts per category. Then I sorted the entry set by value descending and collected into a
`LinkedHashMap` for the final result. `LinkedHashMap` was the deliberate choice because it
preserves insertion order — so after sorting, the caller gets categories ranked
highest-to-lowest. A plain `HashMap` would lose the ordering after collection. A `TreeMap`
would re-sort alphabetically by category name, which is not what the caller needs.

---

## Spring Boot & REST API Design

**5.** The original `POST /api/transactions` endpoint returned a `ResponseEntity<Transaction>`
rather than a `ResponseEntity<TransactionResponse>`. Explain specifically what was wrong
with this. What does a DTO (data transfer object) protect against, and what risks does
returning an entity directly introduce?

The immediate problem is field leakage. Once `Transaction` extends `Auditable`, the entity
carries `createdAt` and `updatedAt`. Return the entity and those fields appear in the JSON
response — fields the API never intended to expose. The caller now depends on them, and you
can't remove them without breaking the contract.

The deeper problem is coupling. The API contract and the persistence model are now the same
thing. Rename a column in the database and the JSON field name changes with it. Add an
internal field to the entity for some implementation reason and it's suddenly in the API.
The DTO exists precisely to break this coupling: it is a stable, intentional description
of what the API exposes, decoupled from how the data is stored. There's also a practical
JPA risk — if any relationship on the entity is lazily loaded, Jackson will try to
serialise it outside the transaction context and throw a `LazyInitializationException`.

---

**6.** When a `POST` request arrives at `TransactionController`, describe the complete
journey from HTTP request to database insert. Name each layer the request passes through,
what each layer is responsible for, and what would happen if the `@Valid` annotation were
removed from the method parameter.

The request arrives at the embedded Tomcat server, which hands it to Spring's
`DispatcherServlet`. The servlet matches the path and method to `createTransaction` in
`TransactionController`. Before the method body executes, Spring's argument resolver
deserialises the JSON body into a `TransactionRequest` object using Jackson. Because
`@Valid` is present, Spring then triggers Bean Validation — it runs all the constraint
annotations (`@NotNull`, `@DecimalMin`, `@NotBlank`) on the request object. If any fail,
Spring returns a 400 before the controller method body even runs.

If the validation passes, the controller calls `service.createTransaction(request)`.
The service maps the request fields onto a `Transaction` entity using the builder, then
calls `repository.save(transaction)`. Spring Data JPA translates that into an INSERT
statement via Hibernate, executes it against the database, and returns the saved entity
with its generated ID populated. The controller wraps it in a `TransactionResponse` DTO
and returns a 201 Created.

If `@Valid` were removed, the constraint annotations on `TransactionRequest` would be
completely ignored. A request with a null `accountId`, a blank `description`, or a
negative `amount` would pass straight through to the service and be persisted without
any check. The database itself might catch a NOT NULL violation and throw an exception,
but you'd get a 500 Internal Server Error instead of a meaningful 400 Bad Request.

---

**7.** Spring Boot uses `@RestController`, `@Service`, and `@Repository` as stereotype
annotations. They all ultimately do the same thing (register a bean). Why does Spring
provide three different annotations instead of one? What does the distinction communicate
to a developer reading the code?

They communicate architectural role. When you see `@Repository` you know you're looking
at the data access layer. When you see `@Service` you know you're looking at business
logic. When you see `@RestController` you know you're looking at the HTTP layer. That
clarity matters when you're reading an unfamiliar codebase at speed — you can orient
yourself without reading the class body.

There's also a functional difference for `@Repository` specifically: Spring wraps it in an
exception translation proxy that converts JPA/JDBC exceptions into Spring's
`DataAccessException` hierarchy. This means you can write exception handling code in the
service layer without coupling it to a specific JPA provider. `@Service` and
`@RestController` don't do this. So it's not purely cosmetic — `@Repository` has a
concrete runtime effect that `@Component` alone does not.

---

**8.** The `GET /api/transactions/monthly-spend` endpoint accepts `year` and `month` as
query parameters. What HTTP status code should this endpoint return if `month=13` is
passed? Who is responsible for validating it — the controller, the service, or Spring
itself — and how would you implement that validation?

It should return 400 Bad Request. The client sent an invalid input — that's a client error,
not a server error, so 4xx is correct. 422 Unprocessable Entity is also a defensible choice
if you want to be precise about it being semantically invalid rather than syntactically
malformed.

Right now, nobody validates it — `month=13` would pass straight through to the service,
call `LocalDate.of(year, 13, 1)`, and throw a `DateTimeException` which would surface as
a 500. The correct fix is to add `@Min(1) @Max(12)` on the `month` parameter and
`@Min(1)` on `year`, and annotate the controller with `@Validated` so Spring triggers
method-level constraint checking. Then add a `@ControllerAdvice` that catches
`ConstraintViolationException` and returns a 400 with a readable error message. This keeps
validation at the entry point — the controller — rather than letting invalid values leak
into the business logic.

---

## Data Access & SQL

**9.** `TransactionRepository` extends `JpaRepository<Transaction, Long>`. Spring Data JPA
can generate a query from a method named `findByAccountId`. Explain the mechanism behind
this — what is Spring doing at startup to turn that method name into SQL? When would you
write a `@Query` annotation instead of relying on derived query methods?

At startup, Spring Data parses the method name using a keyword DSL. It strips the
`findBy` prefix, then splits the remainder on camel-case boundaries and reserved words
like `And`, `Or`, `Between`, `LessThan`. For `findByAccountId` it finds a single
condition — `accountId` — looks up the corresponding field on the `Transaction` entity,
and generates a JPQL query equivalent to `SELECT t FROM Transaction t WHERE
t.accountId = :accountId`. This is all done via reflection and proxy generation at
application startup, not at runtime.

You reach for `@Query` when the method-name DSL can't express what you need. Anything
involving date part extraction (`YEAR()`, `MONTH()`), aggregate functions, joins across
multiple entities, or subqueries has to be a `@Query`. The `findByCategoryAndMonth`
method in this codebase is a concrete example — you cannot express "where month of
transactionDate equals X" using just method names, because the DSL has no concept of
extracting parts of a date. You need JPQL or native SQL for that.

---

**10.** `calculateMonthlySpend` had a bug in the date boundary comparison. Describe the
bug in plain language — what was the incorrect behaviour, what caused it at the code level,
and what kind of test input reliably exposes this class of off-by-one error? Why is this
type of bug particularly common in date/time logic?

In plain language: transactions on the first day of the requested month were being silently
excluded from the result. If you asked for December 2024 spend, a transaction on December
1st would not be counted — only transactions from December 2nd onwards were included.

At the code level, the filter used `t.getTransactionDate().isAfter(startOfMonth)`.
`isAfter` is strictly greater-than. December 1st is not after December 1st — it is equal
to it — so the predicate returned false and the transaction was filtered out. The fix is
`!t.getTransactionDate().isBefore(startOfMonth)`, which means "on or after", i.e. `>=`.

The test input that reliably catches this class of bug is a transaction dated exactly on
the boundary — in this case December 1st. Off-by-one errors in date logic almost always
hide at the edges: the first day, the last day, midnight, end-of-month. This type of bug
is common because `isAfter`, `isBefore`, `>`, `<` all have strict semantics, while
natural language ("in December", "from the start of the month") is implicitly inclusive.
The developer thinks "after the start of December" means "in December" but it actually
means "from December 2nd onwards".

---

**11.** The application uses H2 in-memory for development. Describe exactly what you
would change to point this application at a PostgreSQL database in production. Be specific:
which files, which properties, and which Maven dependency. What is the risk of using
`spring.jpa.hibernate.ddl-auto=create-drop` in production?

In `pom.xml`, replace the H2 dependency with the PostgreSQL JDBC driver:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

In `application.properties` (or a production-specific `application-prod.properties`),
set:
```properties
spring.datasource.url=jdbc:postgresql://host:5432/dbname
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

`create-drop` in production means Hibernate drops and recreates the entire schema on
every application startup and shutdown. Every deploy wipes your data. This is not a
subtle risk — it is catastrophic and irreversible. The correct setting for production is
`validate` (Hibernate checks that the schema matches the entities and fails fast if it
doesn't, but touches nothing) or `none` (Hibernate does nothing, you manage schema
changes yourself with a migration tool like Flyway or Liquibase).

---

## Testing

**12.** `TransactionServiceTest` uses `@Mock` on `TransactionRepository` and
`@InjectMocks` on `TransactionServiceImpl`. Explain what Mockito is doing here. What is
the repository being replaced with, and what does the test actually verify? What category
of bug can this test suite catch — and what category can it not?

`@Mock` tells Mockito to create a fake implementation of `TransactionRepository` — an
object that has all the same methods but does nothing by default unless you tell it what
to return with `when(...).thenReturn(...)`. `@InjectMocks` tells Mockito to create a real
`TransactionServiceImpl` and inject that fake repository into it via the constructor.

What the test actually verifies is the business logic inside `TransactionServiceImpl` in
isolation. When we assert that `calculateMonthlySpend` returns `FOOD = 144.23`, we're
verifying that the filtering and grouping logic is correct — not that any SQL was
executed. The repository is hardcoded to return a fixed list of transactions we control.

This approach catches logic bugs — wrong filter conditions, incorrect grouping, off-by-one
boundaries, wrong accumulator in a reduce. What it cannot catch is anything involving
actual database behaviour: whether the SQL query is correct, whether the schema matches
the entity, whether an index exists, whether a constraint fires. Those require a real
database and an integration test.

---

**13.** A teammate argues that because the service tests cover all the logic, there is no
need to write controller tests. Do you agree? Describe one specific type of bug that a
controller-level test (using `MockMvc`) would catch that the service tests in this project
would miss entirely.

I disagree. Service tests and controller tests verify different things and neither replaces
the other.

A concrete example from this codebase: the `createTransaction` endpoint was returning
`ResponseEntity<Transaction>` — the raw entity — instead of `ResponseEntity<TransactionResponse>`.
The service tests have no opinion about this at all. They test `service.createTransaction()`
which returns a `Transaction` object, which is correct. The bug lives entirely in how the
controller wraps and returns that result. A MockMvc test that asserts `jsonPath("$.createdAt").doesNotExist()`
would catch this immediately. A service test never would, because it never touches the
HTTP layer. Similarly, bean validation (`@Valid`) rejecting a request with a negative
amount is a controller-layer concern — the service tests never send an HTTP request, so
they can't catch a missing `@Valid` annotation.

---

**14.** Looking at the tests you wrote in `TransactionCandidateTest.java`: what was the
first test you wrote, and why did you choose to start there? What does the order in which
you wrote tests tell you about how you approached the problem?

The first test I wrote was `calculateMonthlySpend_includesFirstDayOfMonth`. I started
there because that was the named, critical bug — the one the problem statement called out
explicitly and the one that was causing a broken demo. A test that directly reproduces the
bug you just fixed is the most valuable test you can write: it proves the bug was real,
proves your fix works, and will catch any future regression if someone reintroduces the
same mistake.

The order I wrote tests in reflects how I approached the whole project: bugs first, then
boundary conditions, then edge cases. I wrote regression tests for each bug before writing
general coverage tests. This tells you I was thinking about what could go wrong, not just
about achieving coverage numbers. A test that exists to catch a specific known failure is
worth more than a test that exists to make the coverage report look good.

---

## AI & Modern Engineering

**15.** Describe how you used AI tools during this project. For at least two specific
examples: what did you prompt the tool with, what did it return, and what did you change
or reject? Identify one place where the AI output was immediately trustworthy and one
place where it required meaningful scrutiny before you used it.

I used Chatgpt throughout the project.

**Example 1 — `BudgetCalculator` implementation (required scrutiny):**
I asked Chatgpt to implement `getTopSpendingCategories`. It returned a correct
implementation structurally, but used `TreeMap` as the result container. I caught this
because I know `TreeMap` sorts by key — alphabetical category name — not by value. The
whole point of the method is to return categories ranked by spend amount, so `TreeMap`
would give you the wrong ordering silently. No test would catch it because the test only
checks that the top category is present, not the full order. I replaced it with
`LinkedHashMap` which preserves insertion order after the sort-by-value step.

**Example 2 — `deleteTransaction` fix (required scrutiny):**
Copilot suggested `repository.findById(id).orElseThrow(EntityNotFoundException::new)` as
the existence check before deleting. This works but loads the full entity from the
database just to discard it. I changed it to `repository.existsById(id)` which is a
lightweight existence check — it generates a `SELECT 1` rather than fetching the full
row. Small difference, but it's the kind of thing that matters at scale and it shows
you're thinking about what the code actually does, not just whether it compiles.

The general principle I follow: AI is fast at structure and boilerplate, unreliable at
reasoning about subtle correctness. Use it for the former, verify the latter yourself.