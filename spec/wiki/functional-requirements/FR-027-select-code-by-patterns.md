---
id: FR-027
type: functional-requirement
title: Select code by patterns
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-025-declare-the-architecture-in-a-file]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 2
goal: []
delivery:
delivery-checked:
---

# Select code by patterns

## Rationale

Assigning every package by hand does not scale and drifts as code is added.

## Requirement

A group's `match` lists [[DT-013-selector|selectors]] `<kind>:<glob>`: `module:`, `package:`, `path:`, `type:` with optional `@kind`, and `ext:`. `*` matches within one name segment, `**` across segments, the separator is `.` for names and `/` for paths. A leading `!` excludes what an earlier selector of the same group matched.

## Acceptance criteria

- [ ] `type:**Repository@interface` selects `OrderRepository` but not `InMemoryOrderRepository`.
- [ ] `package:com.example.domain.**` selects every package below `com.example.domain`.

## Hierarchy

- **Parent:** [[FR-025-declare-the-architecture-in-a-file]]
- **Children:** none.
