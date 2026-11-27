---
id: FR-032
type: functional-requirement
title: Report cycles as violations
status: draft
created: 2026-11-27
updated: 2026-11-27
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
order: 3
goal: []
delivery:
delivery-checked:
---

# Report cycles as violations

## Rationale

Cycles should be forbidden at a chosen level, not only highlighted.

## Requirement

A `no-cycles` rule at `level` `package`, `file` or `type`, optionally `within` a group, reports one violation per cycle with its members and the dependencies that close it.

## Acceptance criteria

- [ ] `no-pkg-cycles` on the fixture reports exactly one cycle, `domain` and `service`.

## Hierarchy

- **Parent:** [[FR-029-check-the-code-against-architecture-rules]]
- **Children:** none.
