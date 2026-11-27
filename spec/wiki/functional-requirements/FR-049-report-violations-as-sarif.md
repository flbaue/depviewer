---
id: FR-049
type: functional-requirement
title: Report violations as SARIF
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-048-review-only-what-a-change-touches]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Should
release: unscheduled
lane: backbone
order: 1
goal: []
delivery:
delivery-checked:
---

# Report violations as SARIF

## Rationale

Code-review tools show SARIF results as annotations on the changed lines.

## Requirement

`check` writes SARIF with one result per violation, at its file and line.

## Acceptance criteria

- [ ] A pull request that adds a layer violation shows an annotation at the offending line.

## Hierarchy

- **Parent:** [[FR-048-review-only-what-a-change-touches]]
- **Children:** none.

> [!note] Open points
> Not specified yet.
