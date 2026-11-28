---
id: FR-017
type: functional-requirement
title: Show details and jump to references
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-010-viewer-reference]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-013-explore-the-dependency-structure]]"]
building-blocks: ["[[BB-013-viewer]]"]
priority: Must
release: delivered
lane: backbone
order: 4
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Show details and jump to references

## Rationale

A reader has to get from a suspicious arc to the line of code that causes it.

## Requirement

Hovering highlights a row or arc and the rows it connects. Clicking a row shows kind, location, size, level and its dependencies in both directions, grouped by the visible row on the other side. Clicking an arc lists its references with kind and `file:line`. Clicking a reference jumps to its element.

## Acceptance criteria

- [ ] Clicking the arc from `Order` to `PriceCalculator` lists `Order.total()` → `PriceCalculator.gross(double)` (call) at `Order.java:25`.

## Hierarchy

- **Parent:** [[FR-013-explore-the-dependency-structure]]
- **Children:** none.
