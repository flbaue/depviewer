---
id: GOAL-003
type: goal
title: AI agents check and explain their own changes
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: []
tags: [goal]
stereotype: objective
parent: ["[[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]]"]
beneficiary: ["[[STK-003-ai-coding-agents]]", "[[STK-001-software-architects]]"]
metric: agent finds and explains a violation using only the CLI and its docs
baseline: no check exists (2026-11-27)
target: "yes"
horizon: "release m3-agent-interface"
short_title: Agents check their changes
tile_claim:
---

# AI agents check and explain their own changes

## Objective

- **Purpose.** An AI coding agent finds out whether its change adds a violation and explains the
  dependency path it introduced, using only the CLI and its documentation.
- **Advantage.** [[STK-003-ai-coding-agents|AI coding agents]] keep to the architecture without a human pointing out
  each violation in review, and [[STK-001-software-architects|architects]] get changes that already respect the rules.
- **Metric.** The agent completes this task with only the CLI and its documentation.

**Parent.** see `parent:`.

## Impact

- **Addressed stakeholders:** see `beneficiary:`.
- **Served by:** projected from the FR backlinks.
