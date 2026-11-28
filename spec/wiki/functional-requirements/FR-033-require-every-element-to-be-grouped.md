---
id: FR-033
type: functional-requirement
title: Require every element to be grouped
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
priority: Should
release: m1-rules-and-check
lane: backbone
order: 4
goal: []
delivery:
delivery-checked:
---

# Require every element to be grouped

## Rationale

Code that belongs to no group escapes every other rule.

## Requirement

A `no-ungrouped` rule reports every element in its optional `scope` selector that is in no group.

## Acceptance criteria

- [ ] A new package `com.example.misc` with no matching group is reported by `no-ungrouped`.

## Hierarchy

- **Parent:** [[FR-029-check-the-code-against-architecture-rules]]
- **Children:** none.
