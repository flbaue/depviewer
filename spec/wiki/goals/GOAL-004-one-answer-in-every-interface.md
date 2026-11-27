---
id: GOAL-004
type: goal
title: One answer in every interface
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: []
tags: [goal]
stereotype: objective
parent: ["[[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]]"]
beneficiary: ["[[STK-001-software-architects]]", "[[STK-002-developers]]", "[[STK-003-ai-coding-agents]]"]
metric: violations that differ between viewer and CLI
baseline:
target: "0"
horizon:
short_title: Same result everywhere
tile_claim:
---

# One answer in every interface

## Objective

- **Purpose.** Viewer, CLI and agent interface read the same scan model and the same architecture
  file and report the same violations.
- **Advantage.** Nobody has to decide which of two answers is right: what the
  [[STK-001-software-architects|architect]] sees in the viewer is what CI and the [[STK-003-ai-coding-agents|agent]] enforce.
- **Metric.** Violations that differ between viewer and CLI on the same model and file.

**Parent.** see `parent:`.

## Impact

- **Addressed stakeholders:** see `beneficiary:`.
- **Served by:** projected from the FR backlinks.
