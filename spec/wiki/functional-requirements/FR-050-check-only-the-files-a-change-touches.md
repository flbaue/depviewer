---
id: FR-050
type: functional-requirement
title: Check only the files a change touches
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-048-review-only-what-a-change-touches]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Could
release: unscheduled
lane: backbone
order: 2
goal: []
delivery:
delivery-checked:
---

# Check only the files a change touches

## Rationale

Checking only changed files is faster and gives a focused result.

## Requirement

`check` takes a list of changed files and reports only violations whose source is in them.

## Acceptance criteria

- [ ] Given only `Order.java`, the fixture reports `layering` and nothing from other files.

## Hierarchy

- **Parent:** [[FR-048-review-only-what-a-change-touches]]
- **Children:** none.

> [!note] Open points
> Not specified yet.
