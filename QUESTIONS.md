# Questions

Answer each question in your own words. Aim for three to eight sentences per answer —
enough to show that you understand the concept, not so much that you are restating a
textbook entry. We are looking for clarity of thinking, not exhaustive coverage.

---

## Java & Object-Oriented Design

**1.** The `Auditable` abstract class in this codebase uses `@MappedSuperclass`. Explain
what this annotation tells JPA, and describe what would happen to the database schema if
you removed it. Why is it better to put `createdAt` and `updatedAt` in a shared abstract
class rather than adding those fields directly to `Transaction` and `Account` separately?



**2.** `TransactionService` is defined as a Java interface, with `TransactionServiceImpl`
as its only implementation. A new engineer on the team asks: "Why bother with the interface
if there's only one implementation? Isn't it just extra boilerplate?" How do you respond?
Give at least one concrete scenario where the interface pays off.



**3.** `Category` is modelled as an enum rather than a plain `String` field on
`Transaction`. What does storing it as `@Enumerated(EnumType.STRING)` in the database
actually produce in the table? What would go wrong if a future developer added a new
category value to the enum but forgot to handle database migration?



**4.** `BudgetCalculator` is a `final` class with a private constructor and a single
static method. What pattern is this, and why is it appropriate for this specific utility?
In your implementation, what data structure did you use as an intermediate step before
building the final sorted map, and why?



---

## Spring Boot & REST API Design

**5.** The original `POST /api/transactions` endpoint returned a `ResponseEntity<Transaction>`
rather than a `ResponseEntity<TransactionResponse>`. Explain specifically what was wrong
with this. What does a DTO (data transfer object) protect against, and what risks does
returning an entity directly introduce?



**6.** When a `POST` request arrives at `TransactionController`, describe the complete
journey from HTTP request to database insert. Name each layer the request passes through,
what each layer is responsible for, and what would happen if the `@Valid` annotation were
removed from the method parameter.



**7.** Spring Boot uses `@RestController`, `@Service`, and `@Repository` as stereotype
annotations. They all ultimately do the same thing (register a bean). Why does Spring
provide three different annotations instead of one? What does the distinction communicate
to a developer reading the code?



**8.** The `GET /api/transactions/monthly-spend` endpoint accepts `year` and `month` as
query parameters. What HTTP status code should this endpoint return if `month=13` is
passed? Who is responsible for validating it — the controller, the service, or Spring
itself — and how would you implement that validation?



---

## Data Access & SQL

**9.** `TransactionRepository` extends `JpaRepository<Transaction, Long>`. Spring Data JPA
can generate a query from a method named `findByAccountId`. Explain the mechanism behind
this — what is Spring doing at startup to turn that method name into SQL? When would you
write a `@Query` annotation instead of relying on derived query methods?



**10.** `calculateMonthlySpend` had a bug in the date boundary comparison. Describe the
bug in plain language — what was the incorrect behaviour, what caused it at the code level,
and what kind of test input reliably exposes this class of off-by-one error? Why is this
type of bug particularly common in date/time logic?



**11.** The application uses H2 in-memory for development. Describe exactly what you
would change to point this application at a PostgreSQL database in production. Be specific:
which files, which properties, and which Maven dependency. What is the risk of using
`spring.jpa.hibernate.ddl-auto=create-drop` in production?



---

## Testing

**12.** `TransactionServiceTest` uses `@Mock` on `TransactionRepository` and
`@InjectMocks` on `TransactionServiceImpl`. Explain what Mockito is doing here. What is
the repository being replaced with, and what does the test actually verify? What category
of bug can this test suite catch — and what category can it not?



**13.** A teammate argues that because the service tests cover all the logic, there is no
need to write controller tests. Do you agree? Describe one specific type of bug that a
controller-level test (using `MockMvc`) would catch that the service tests in this project
would miss entirely.



**14.** Looking at the tests you wrote in `TransactionCandidateTest.java`: what was the
first test you wrote, and why did you choose to start there? What does the order in which
you wrote tests tell you about how you approached the problem?



---

## AI & Modern Engineering

**15.** Describe how you used AI tools during this project. For at least two specific
examples: what did you prompt the tool with, what did it return, and what did you change
or reject? Identify one place where the AI output was immediately trustworthy and one
place where it required meaningful scrutiny before you used it.
