---
id: FR-045
type: functional-requirement
title: "Query dependents, dependencies, paths and cycles"
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-044-let-ai-agents-query-and-check-the-architecture]]"]
building-blocks: ["[[BB-014-query-commands]]"]
priority: Should
release: m3-agent-interface
lane: backbone
order: 1
goal: []
delivery:
delivery-checked:
---

# Query dependents, dependencies, paths and cycles

## Rationale

An agent asks "may I depend on X from here?" and "why does A depend on B?".

## Requirement

Commands `dependents`, `dependencies`, `path` and `cycles` answer from a scan or saved model, with text and JSON output.

## Acceptance criteria

- [ ] `path` from `com.example.domain` to `com.example.service` on the fixture returns the `Order.total()` → `PriceCalculator.gross(double)` reference.

## Hierarchy

- **Parent:** [[FR-044-let-ai-agents-query-and-check-the-architecture]]
- **Children:** none.

> [!note] Open points
> Command syntax and JSON format are not specified yet.
