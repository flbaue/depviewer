---
id: GOAL-002
type: goal
title: Teams hold new code to their architecture in CI
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: ["[[ISS-011-how-long-may-the-check-take]]"]
tags: [goal]
stereotype: objective
parent: ["[[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]]"]
beneficiary: ["[[STK-001-software-architects]]", "[[STK-002-developers]]"]
metric: check duration on a real project with tens of thousands of dependencies
baseline:
target: about the duration of a scan
horizon:
short_title: New violations fail CI
tile_claim:
---

# Teams hold new code to their architecture in CI

## Objective

- **Purpose.** A team describes its layers or bounded contexts in one file and gets a CI check that
  fails only on new violations, on a real project with tens of thousands of dependencies.
- **Advantage.** [[STK-001-software-architects|Architects]] and [[STK-002-developers|developers]] can adopt rules on legacy code
  without fixing every old violation first, and ratchet the baseline down later.
- **Metric.** Duration of the check on a real project, compared with a scan of the same project.

**Parent.** see `parent:`.

## Impact

- **Addressed stakeholders:** see `beneficiary:`.
- **Served by:** projected from the FR backlinks.

> [!note] Open points
> "About the time of a scan" is not a number yet, see [[ISS-011-how-long-may-the-check-take]].
