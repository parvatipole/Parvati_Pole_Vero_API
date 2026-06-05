# Decisions

**Name: Parvati Pole**

**Date started: 05/06/2026 at 7:46pmIST**

**Date submitted: 06/06/2026 at 12:46amIST**

My approach was to read everything before writing a single line. I started with
`TransactionServiceTest.java` because tests are a specification — they describe exactly
what the code is supposed to do, including what the bugs are. From there I read the
controller, service interface, DTOs, and entities top to bottom. I prioritised finding
every broken or missing piece before touching anything, so I had a complete picture of
the damage before deciding what to fix first.
 
---
 
## 1. Code & Design Decisions
 
**The codebase includes an `Auditable` abstract class that is not currently used by any
entity. What did you do with it, if anything? Walk through your reasoning — what is the
purpose of the Auditable pattern, what are the tradeoffs of using it versus not, and why
did you make the choice you did?**
 
The `Auditable` class was defined with `@MappedSuperclass` and `@PrePersist`/`@PreUpdate`
lifecycle hooks but neither `Transaction` nor `Account` extended it — the class existed
but had zero effect. I identified this as Bug 5 and applied it: both entities now extend
`Auditable`.
 
The purpose of the pattern is to centralise `created_at` / `updated_at` timestamp
management. Without it, every entity that needs auditing has to duplicate the same two
columns and the same lifecycle wiring. `@MappedSuperclass` means JPA flattens those
columns directly into each entity's table at schema generation time — no join, no
separate table, no runtime overhead.
 
The tradeoff is a shallow inheritance hierarchy in the domain layer. That's generally
acceptable in Spring/JPA codebases where entities are data carriers rather than
behaviour-rich objects. The only real risk is if different entities later need different
auditing behaviour (e.g. timezone-aware timestamps, or a `deleted_at` soft-delete
column for one entity only) — at that point the shared base becomes a constraint rather
than a convenience. For this codebase that risk is low and the consistency benefit is
worth it.
 
**`TransactionResponse` is used as the outbound DTO for the API. What changes did you
make to it, if any? Why does the shape of a response DTO matter — and what is the risk
of returning an entity directly from a controller?**
 
I found two separate problems here.
 
First, `TransactionResponse.fromTransaction` was silently dropping `category` and
`accountId` — the builder mapped only `id`, `amount`, `description`, and
`transactionDate`. Every API response was missing two fields the caller needs. I fixed
`fromTransaction` to map all six fields.
 
Second, the `createTransaction` endpoint in the controller was declared as
`ResponseEntity<Transaction>` — returning the raw JPA entity. I changed the return type
to `ResponseEntity<TransactionResponse>` so the response is shaped by the DTO contract.
 
Returning an entity directly has several concrete risks. When `Auditable` is applied
(as I did), the entity now carries `createdAt` and `updatedAt`. Returning the entity
exposes those fields in the JSON response — they are server-internal and should not be
part of the API contract. Beyond leaking internal fields, returning a JPA entity risks
`LazyInitializationException` during Jackson serialisation if any relationships are
lazily loaded. It also couples the API shape directly to the persistence model: rename
a column in the DB and the API contract silently changes with it.
 
**The `BudgetCalculator` requires grouping and sorting data. What data structure or
approach did you choose to implement it? Walk through the alternatives you considered
and why you landed where you did.**
 
`TransactionServiceImpl` was referencing `com.vero.api.util.BudgetCalculator` but that
class did not exist anywhere in the codebase. It was a compile error. I created it.
 
The implementation:
1. Groups transactions by category using `Collectors.groupingBy` + `Collectors.reducing`
   to sum `BigDecimal` amounts per group.
2. Sorts the entry set descending by total value.
3. Takes the top N entries with `limit(topN)`.
4. Collects into a `LinkedHashMap` to preserve insertion order.
`LinkedHashMap` is the deliberate choice here. A plain `HashMap` loses ordering after
collection, defeating the point of sorting. A `TreeMap` re-sorts by key (alphabetical
category name), not by value — so the caller would get categories in name order, not
spend order. `LinkedHashMap` preserves insertion order, meaning the caller receives
categories ranked highest-to-lowest as the method contract requires.
 
**Were there any decisions you made that are not covered by the questions above? Describe
the most significant one and your reasoning.**
 
The most significant was fixing `findByCategoryAndMonth` in `TransactionRepository`.
`TransactionServiceImpl.getCategoryTransactionsForMonth` was calling a repository method
that didn't exist — not as a derived query method, not as a `@Query`. This would cause
a Spring Data startup failure as soon as the application context tried to create the
repository bean.
 
I added the method with a proper JPQL `@Query` annotation:
```java
@Query("SELECT t FROM Transaction t WHERE t.category = :category " +
       "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
List<Transaction> findByCategoryAndMonth(
    @Param("category") Category category,
    @Param("year") int year,
    @Param("month") int month);
```
 
A Spring Data derived method name cannot express month/year extraction from a date
column — the method-name DSL only supports field equality and range queries, not date
part extraction. A `@Query` is the correct approach here.
 
---
 
## 2. Bug Fixes & Issues Found
 
**Describe each problem you found in the codebase. For each one: where was it, how did
you identify it, what did it cause, and how did you fix it?**
 
**Bug 1 — Off-by-one in `calculateMonthlySpend` (CRITICAL)**
File: `TransactionServiceImpl.java`
 
Original:
```java
.filter(t -> t.getTransactionDate().isAfter(startOfMonth) && ...)
```
`isAfter` is strictly greater-than. For December 2024, `startOfMonth` is Dec 1. A
transaction on Dec 1 is not after Dec 1 — it is equal to it — so it was silently
excluded. `TransactionServiceTest.testCalculateMonthlySpend_includesFirstDayOfMonth`
names this bug explicitly and asserts `FOOD = 144.23`, which requires the Dec 1
transaction (87.45) to be included.
 
Fix: `!t.getTransactionDate().isBefore(startOfMonth)` which is `>=` (inclusive).
 
**Bug 2 — `createTransaction` returns raw entity instead of DTO**
File: `TransactionController.java`
 
Return type was `ResponseEntity<Transaction>`. Changed to
`ResponseEntity<TransactionResponse>`. See Section 1 above for full reasoning.
 
**Bug 3 — `TransactionResponse.fromTransaction` drops `category` and `accountId`**
File: `TransactionResponse.java`
 
The `fromTransaction` builder was missing `.category(t.getCategory())` and
`.accountId(t.getAccountId())`. Every API response silently omitted two fields. Fixed
by adding the missing mappings.
 
**Bug 4 — `BudgetCalculator` referenced but never existed (compile error)**
File: `TransactionServiceImpl.java` + missing `BudgetCalculator.java`
 
`getTopSpendingCategories` delegated to a utility class that didn't exist. The project
would not compile. Created `BudgetCalculator.java` with the full implementation.
 
**Bug 5 — `Auditable` defined but never applied to any entity**
Files: `Transaction.java`, `Account.java`
 
Both entities were missing `extends Auditable`. The auditing infrastructure existed but
had no effect. Fixed by applying the superclass to both entities.
 
**Bug 6 — `findByCategoryAndMonth` does not exist in repository**
File: `TransactionServiceImpl.java`, `TransactionRepository.java`
 
`getCategoryTransactionsForMonth` called a repository method that wasn't defined.
Spring Data would fail at startup. Fixed by adding the method with a `@Query` JPQL
annotation.
 
**Bug 7 — `getTransactionsByDateRange` was a permanent stub**
File: `TransactionServiceImpl.java`
 
The method returned `Collections.emptyList()` unconditionally with a TODO comment.
Fixed by implementing a stream filter with inclusive boundary checks on both ends:
`!isBefore(startDate) && !isAfter(endDate)`.
 
**Bug 8 — `deleteTransaction` returns 204 for non-existent IDs**
File: `TransactionServiceImpl.java`
 
Spring Data's `deleteById` is a silent no-op when the ID doesn't exist — it does not
throw. The controller had a `try-catch` for `EntityNotFoundException` that could never
trigger. Fixed by explicitly checking existence first:
```java
if (!repository.existsById(id)) {
    throw new EntityNotFoundException("Transaction not found with id: " + id);
}
repository.deleteById(id);
```
 
**Were there any problems you noticed but chose not to fix? If so, explain why.**
 
`calculateMonthlySpend` and `getTransactionsByDateRange` both call `repository.findAll()`
and filter in Java. This loads the entire table into heap memory on every request — a
correctness risk at scale, not just a performance issue. I did not fix this because it
requires adding a `@Query` to the repository and the current in-memory approach is
functionally correct for the scope of this exercise. I have noted it as the top priority
in Section 5.
 
---
 
## 3. Testing Decisions
 
**What tests did you write in `TransactionCandidateTest.java`? For each test, explain
what behaviour it validates and why you chose to cover that behaviour.**
 
I wrote 17 tests at the service unit-test level (mocked repository), focused on the
bugs I fixed and the boundary conditions they created:
 
| Test | Behaviour validated |
|------|---------------------|
| `calculateMonthlySpend_includesFirstDayOfMonth` | Dec 1 transaction IS included — direct regression test for Bug 1 |
| `calculateMonthlySpend_includesLastDayOfMonth` | Dec 31 transaction IS included — end boundary parity |
| `calculateMonthlySpend_excludesPreviousMonth` | Nov transactions do NOT appear in Dec result |
| `calculateMonthlySpend_excludesNextMonth` | Jan transactions do NOT appear in Dec result |
| `calculateMonthlySpend_emptyWhenNoTransactionsInMonth` | Month with no data returns empty map, not error |
| `calculateMonthlySpend_sumsCorrectlyByCategory` | Grouping and summing across multiple categories |
| `getTransactionsByDateRange_inclusiveOnStartDate` | Start date transaction IS included |
| `getTransactionsByDateRange_inclusiveOnEndDate` | End date transaction IS included |
| `getTransactionsByDateRange_excludesOutsideRange` | Transactions outside range are excluded |
| `getTransactionsByDateRange_emptyWhenNoMatch` | No matches returns empty list |
| `getTopSpendingCategories_respectsTopNLimit` | Result never exceeds topN |
| `getTopSpendingCategories_sortsDescendingByAmount` | Highest spend category is first |
| `getTopSpendingCategories_emptyInputReturnsEmptyMap` | Empty transaction list is handled |
| `createTransaction_persistsAndReturns` | Service maps request to entity and saves |
| `deleteTransaction_whenNotFound_throwsEntityNotFoundException` | Missing ID throws, not silent — Bug 8 regression |
| `getTransactionById_whenFound_returnsTransaction` | ID lookup returns wrapped value |
| `getTransactionById_whenNotFound_returnsEmpty` | Missing ID returns empty Optional |
 
**What did you deliberately not test, and why? If you had more time, what would be the
next most important test to add?**
 
I did not write controller-layer (MockMvc) tests. The existing `TransactionServiceTest`
and my tests together cover business logic thoroughly. What's missing is a slice test
that validates HTTP concerns: status codes, JSON shape, bean validation rejection, and
that the response DTO does not expose `createdAt`/`updatedAt`. That would be the next
test layer to add.
 
After that, a full `@SpringBootTest` integration test against H2 would catch wiring
issues — missing beans, schema mismatches — that no unit test can detect.
 
**What is the difference between what `TransactionServiceTest` covers and what your
`TransactionCandidateTest` covers? Are they testing the same things?**
 
They overlap in structure but focus on different bugs and boundary conditions.
`TransactionServiceTest` was written before my changes and tests the baseline happy
paths (get all, get by ID, create, basic monthly spend). My tests in
`TransactionCandidateTest` are regression-focused: every bug I fixed has a dedicated
test that would have failed against the original code. I also added boundary tests
(`includesFirstDay`, `includesLastDay`, `dateRange` inclusivity on both ends) that
`TransactionServiceTest` does not cover. The two files are complementary, not
duplicates.
 
---
 
## 4. AI Tool Usage
 
**Which AI tools did you use?**
 
Chatgpt,Copilot.
 
**Give two or three specific examples of how you used AI on this project.**
 

1. **`BudgetCalculator` implementation** — Chatgpt suggested `TreeMap` as the result
   container. I rejected this: `TreeMap` sorts by key (alphabetical category name), not
   by value, so the caller would not get categories in spend order. I changed it to
   `LinkedHashMap` which preserves insertion order after the sort-by-value step.
2. **`@Query` for `findByCategoryAndMonth`** — Chatgpt drafted the JPQL query. I
   reviewed it to confirm that Spring Data's derived method DSL genuinely cannot express
   month/year extraction (it can't — it requires `@Query`), and that `YEAR()` and
   `MONTH()` are valid JPQL functions in Hibernate. They are.
**Describe a moment where AI gave you something wrong, incomplete, or subtly misleading.**
 
The `deleteTransaction` fix. Chatgpt's first suggestion was:
```java
repository.findById(id).orElseThrow(EntityNotFoundException::new);
repository.deleteById(id);
```
This makes two database round trips and loads the full entity just to discard it. I
changed it to `existsById` + `deleteById` — also two trips, but the first one is a
lightweight existence check rather than a full row fetch. For a delete operation you
don't need the entity; you only need to know whether it exists. The `findById` approach
is a common pattern AI defaults to because it appears frequently in training data, but
it's wasteful here.
 
**What is your general philosophy on using AI when writing backend code?**
 
AI accelerates the structural work — generating test stubs, writing boilerplate mapping
code, producing first drafts of JPQL queries. It is unreliable at reasoning about subtle
correctness: boundary conditions, the difference between `isAfter` and `!isBefore`,
what `TreeMap` vs `LinkedHashMap` actually does to ordering. I treat AI output as a
first draft that I read and verify before accepting, the same way I would treat a PR
from a junior developer. The rule I follow: if I cannot explain why a piece of code is
correct without running it, I don't submit it.
 
---
 
## 5. What You'd Do Next
 
**If you had two more days on this project, what would you build or fix first?**
 
1. **Replace `findAll()` + in-memory filter in `calculateMonthlySpend` and
   `getTransactionsByDateRange` with proper repository queries** — these load the entire
   table into heap on every request. The fix is `findByTransactionDateBetween` or a
   `@Query` in the repository. This is a correctness risk at any meaningful data volume,
   not just a performance issue.
2. **Add MockMvc controller-layer tests** — validate HTTP status codes, JSON shape,
   bean validation rejection (missing fields, negative amount), and that `createdAt`/
   `updatedAt` are absent from responses.
3. **Add a `@SpringBootTest` integration test against H2** — neither layer of unit
   tests catches wiring bugs or schema mismatches. An end-to-end test that creates a
   transaction and reads it back through the full stack would catch those.
4. **Add `@ControllerAdvice` for global exception handling** — `EntityNotFoundException`
   is currently caught inside the controller method. A global handler centralises error
   response shaping and HTTP status mapping, making it consistent when new exception
   types are added.
5. **Expose `getTransactionsByDateRange` as an HTTP endpoint** — the service method
   now works correctly but there is no route in the controller that calls it.
**What is the biggest remaining risk or weakness in the code you have submitted?**
 
The `findAll()` + in-memory filter pattern in `calculateMonthlySpend`. It is
functionally correct for small datasets but will load an unbounded number of rows into
memory on every request as the table grows. 