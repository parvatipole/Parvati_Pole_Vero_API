# Vero API

## Welcome to Vero

Vero is a small personal finance startup. We help individuals track their transactions,
categorise their spending, and understand where their money goes each month. Our backend
is a Spring Boot REST API; our frontend is a thin client that lives in a separate repo.
The team is three backend engineers and one product lead. We move quickly, we ship to
real users, and we are at the stage where the shape of what we build now will define
what the company can build next year.

You are joining mid-stride. The engineer who was building the transaction management
module left unexpectedly three weeks ago. Some of his code is complete. Some is stubbed
out with TODOs. Some has quiet bugs that no one has caught because the coverage that
would have caught them was never written. The monthly spend report is the visible piece —
it does not behave correctly right now, and we have a customer demo for a potential
enterprise client in one week.

This is your codebase now. Read it, understand it, and put it in a state you would be
comfortable handing to the next engineer who joins after you.

---

## The Application

A REST API for managing personal financial transactions. Each transaction belongs to an
account, has an amount, a description, a category (FOOD, TRANSPORT, UTILITIES, etc.),
and a date. The API supports listing, filtering, creating, deleting, and aggregating
spend by month.

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/transactions | List all transactions |
| GET | /api/transactions/{id} | Get a single transaction by ID |
| GET | /api/transactions/account/{accountId} | List transactions for an account |
| GET | /api/transactions/monthly-spend | Monthly spend by category (params: year, month) |
| POST | /api/transactions | Create a new transaction |
| DELETE | /api/transactions/{id} | Delete a transaction |

### Getting Started

Prerequisites:
- Java 17 JDK (verify: `java -version`)
- Git

To start the application:
```
./mvnw spring-boot:run        (macOS / Linux)
mvnw.cmd spring-boot:run      (Windows)
```

The API will be available at `http://localhost:8080`.
The H2 database console is at `http://localhost:8080/h2-console`.
- JDBC URL: `jdbc:h2:mem:verodb`
- Username: `sa`
- Password: (leave blank)

To run the suite:
```
./mvnw test
```

The first run will download dependencies (~2 minutes). Subsequent runs are fast.

---

## Your Scope

You own the backend code. Infrastructure, deployment, and devops are someone else's
concern this week. What matters is that the application behaves correctly, the code is
clean and maintainable, and the behaviour is covered by checks that would catch a
regression. The codebase you have inherited is a mix of complete, incomplete, and
subtly incorrect work. Treat it the way you would any codebase on your first week:
read it, understand it, and improve it.

AI tools are part of modern engineering — use them. What we are evaluating is your
judgment, not your ability to produce output without assistance.

---

## What We Care About

We care about how you think more than whether you found every issue. The code you
received is imperfect on purpose — real codebases always are. We want to see whether
you can read unfamiliar code, identify what is wrong, make decisions you can defend,
and leave the system in better shape than you found it. Document your reasoning.
That matters as much as the code itself.

---

## Submitting Your Work

1. Fork this repository.
2. Commit your work incrementally — please provide meaningful commit messages, not one big push at the end.
3. Fill out `DECISIONS.md` and `QUESTIONS.md`.
4. Send us the link to your fork when you are done.

Your commit history is part of your submission. It shows how you work, not just what
you produced.
