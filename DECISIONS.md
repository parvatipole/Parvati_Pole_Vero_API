# Decisions

**Name:**

**Date started:**

**Date submitted:**

In two or three sentences, describe your overall approach before getting into specifics.
What did you read first? What did you prioritise, and why?

---

## 1. Code & Design Decisions

**The codebase includes an `Auditable` abstract class that is not currently used by any
entity. What did you do with it, if anything? Walk through your reasoning — what is the
purpose of the Auditable pattern, what are the tradeoffs of using it versus not, and why
did you make the choice you did?**



**`TransactionResponse` is used as the outbound DTO for the API. What changes did you
make to it, if any? Why does the shape of a response DTO matter — and what is the risk
of returning an entity directly from a controller?**



**The `BudgetCalculator` requires grouping and sorting data. What data structure or
approach did you choose to implement it? Walk through the alternatives you considered
and why you landed where you did.**



**Were there any decisions you made that are not covered by the questions above? Describe
the most significant one and your reasoning.**



---

## 2. Bug Fixes & Issues Found

**Describe each problem you found in the codebase. For each one: where was it, how did
you identify it, what did it cause, and how did you fix it?**



**Were there any problems you noticed but chose not to fix? If so, explain why.**



---

## 3. Testing Decisions

**What tests did you write in `TransactionCandidateTest.java`? For each test, explain
what behaviour it validates and why you chose to cover that behaviour.**



**What did you deliberately not test, and why? If you had more time, what would be the
next most important test to add?**



**What is the difference between what `TransactionServiceTest` covers and what your
`TransactionCandidateTest` covers? Are they testing the same things?**



---

## 4. AI Tool Usage

AI tool usage is expected and encouraged. Using AI is not cheating — it is a core skill
of modern engineering. What we are evaluating is whether you used it thoughtfully:
whether you understood and verified what it produced, and whether you can recognise
when its output should not be trusted.

**Which AI tools did you use? (e.g. ChatGPT, Claude, GitHub Copilot, Cursor, other)**



**Give two or three specific examples of how you used AI on this project. For each:
what did you prompt it with, what did it return, and what did you accept, change, or
reject?**



**Describe a moment where AI gave you something wrong, incomplete, or subtly misleading.
How did you catch it, and what did you do?**



**What is your general philosophy on using AI when writing backend code? Where does it
help, and where do you not trust it?**



---

## 5. What You'd Do Next

**If you had two more days on this project, what would you build or fix first?
List in priority order, with one sentence of justification for each.**



**What is the biggest remaining risk or weakness in the code you have submitted?**
