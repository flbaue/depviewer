---
id: FR-034
type: functional-requirement
title: Exempt test sources by default
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-029-check-the-code-against-architecture-rules]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 5
goal: []
delivery: implemented
delivery-checked: "2026-11-28T09:08:23Z"
---

# Exempt test sources by default

## Rationale

Tests often reach across layers on purpose.

## Requirement

Test sources are not checked unless a rule sets `"tests": true`.

## Acceptance criteria

- [ ] A violating reference in `src/test/` is not reported, and is reported with `"tests": true`.

## Hierarchy

- **Parent:** [[FR-029-check-the-code-against-architecture-rules]]
- **Children:** none.
