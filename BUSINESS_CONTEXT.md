# Business Context

## Who Vero Is

Vero is a small fintech startup focused on personal finance management. Our product
helps individual users connect their accounts, see their transactions in one place,
and understand their spending patterns month over month. We launched a year ago. We
have real users — not many, but enough that what we ship reaches actual people, and
when something is broken, someone notices.

We are at an early but critical point. We have a product, we have customers, and we
have proof that what we are building matters to them. We do not yet have the kind of
engineering discipline that a larger company would. The codebase is showing the strain
of moving fast in the first year, and we are starting to feel where that strain costs
us.

## What Is at Stake

A potential enterprise client wants to see the monthly spend report in action during a
demo next week. The feature is not behaving correctly. The team knows it. No one has
had time to sit down and fix it properly. If the demo goes well, this client could
roughly double our revenue. If it does not, we lose months of pipeline work.

The codebase is starting to accumulate shortcuts. The engineer who was building this
module moved fast and left behind code that works for the happy path but has edges
that have not been exercised. The original work was solid in concept; it is the
follow-through that is missing. Technical debt is starting to slow us down.

The product roadmap has three features planned for Q1. None of them can be built
cleanly on top of a transaction module that has known gaps. The foundation needs to
be solid before the next phase starts. We would rather take the time to do this right
now than build three new features on top of a shaky base.

## What Good Looks Like

Not a perfect system. Not a complete rewrite. The monthly spend feature behaving
correctly, the gaps addressed where they matter, the decisions documented so the next
person to touch this code can understand why things are the way they are, and the
codebase in a state where the next engineer can pick it up without doing archaeology
to figure out what is intentional and what is a mistake.

## What We Are Not Asking For

No Spring Security. No caching layer. No microservices split. No deployment pipeline.
Fix what is broken, complete what is incomplete, document what you decided and why.
