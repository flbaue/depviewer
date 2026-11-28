---
id: GOAL-005
type: goal
title: The real structure of a code base at a glance
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-005-readme-of-depviewer]]"]
related: ["[[ISS-013-how-is-structure-at-a-glance-measured]]"]
tags: [goal]
stereotype: objective
parent: ["[[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]]"]
beneficiary: ["[[STK-001-software-architects]]", "[[STK-002-developers]]"]
metric: steps from a checkout to a viewer that marks every package cycle
baseline: one command with only a JDK, cycles marked on first open (2026-11-27)
target: one depviewer command with only a JDK, every package cycle marked on first open without a click
horizon: "release delivered"
short_title: Structure at a glance
tile_claim:
---

# The real structure of a code base at a glance

## Objective

- **Purpose.** Anyone can open one file and see how a code base is really structured: cycles and
  layer violations at package, file, type or member level, down to the exact `file:line`.
- **Advantage.** [[STK-001-software-architects|Architects]] find where the design erodes, and a team agrees on its
  architecture looking at the same picture, without installing anything.
- **Metric.** Steps from a checkout to a viewer that marks every package cycle. Target: one `depviewer` command with only a JDK, and the cycles are marked on first open without a click ([[ISS-013-how-is-structure-at-a-glance-measured]]).

**Parent.** see `parent:`.

## Impact

- **Addressed stakeholders:** see `beneficiary:`.
- **Served by:** projected from the FR backlinks.
