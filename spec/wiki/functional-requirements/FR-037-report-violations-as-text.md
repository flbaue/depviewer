---
id: FR-037
type: functional-requirement
title: Report violations as text
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-035-run-the-check-from-the-command-line-and-in-ci]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 2
goal: []
delivery:
delivery-checked:
---

# Report violations as text

## Rationale

Developers read the result in a terminal.

## Requirement

One violation per rule and pair of innermost elements, with all its lines, sorted by file and line: `path:line  severity  rule  from -> to`, then one indented line per reference with its kind.

## Acceptance criteria

- [ ] The fixture prints `src/main/java/com/example/domain/Order.java:25  error  layering  dom -> svc` followed by the `Order.total()` → `PriceCalculator.gross(double)` (call) line.

## Hierarchy

- **Parent:** [[FR-035-run-the-check-from-the-command-line-and-in-ci]]
- **Children:** none.
