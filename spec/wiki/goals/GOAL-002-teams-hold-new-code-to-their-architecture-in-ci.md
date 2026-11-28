---
id: GOAL-002
type: goal
title: Teams hold new code to their architecture in CI
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: ["[[ISS-011-how-long-may-the-check-take]]"]
tags: [goal]
stereotype: objective
parent: ["[[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]]"]
beneficiary: ["[[STK-001-software-architects]]", "[[STK-002-developers]]"]
metric: check duration on a real project with tens of thousands of dependencies
baseline: no check exists (2026-11-27)
target: at most 1.2 times the duration of a scan
horizon: "release m1-rules-and-check"
short_title: New violations fail CI
tile_claim:
---

# Teams hold new code to their architecture in CI

## Objective

- **Purpose.** A team describes its layers or bounded contexts in one file and gets a CI check that
  fails only on new violations, on a real project with tens of thousands of dependencies.
- **Advantage.** [[STK-001-software-architects|Architects]] and [[STK-002-developers|developers]] can adopt rules on legacy code
  without fixing every old violation first, and ratchet the baseline down later.
- **Metric.** Duration of the check on a real project, compared with a scan of the same project. Measured by [[QR-003-the-check-takes-about-as-long-as-a-scan]].

**Parent.** see `parent:`.

## Impact

- **Addressed stakeholders:** see `beneficiary:`.
- **Served by:** projected from the FR backlinks.
