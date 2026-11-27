---
id: FR-030
type: functional-requirement
title: Enforce a layer order
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[ISS-003-how-does-ungrouped-code-relate-to-a-layers-rule]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-029-check-the-code-against-architecture-rules]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 1
goal: []
delivery:
delivery-checked:
---

# Enforce a layer order

## Rationale

Layering is the most common architecture rule.

## Requirement

A `layers` rule lists groups top to bottom in `order`. A dependency from a lower to a higher layer is a violation. With `strict: true`, skipping a layer is one too. Ungrouped code is outside every layer, so a dependency from or to it is never a `layering` violation ([[ISS-003-how-does-ungrouped-code-relate-to-a-layers-rule]]). A `no-ungrouped` rule makes every file belong to a group.

## Acceptance criteria

- [ ] On the fixture's Layers view, `Order.total()` calling `PriceCalculator.gross` at `Order.java:25` is the only `layering` violation.
- [ ] A dependency between ungrouped code and a layer in either direction gives no `layering` violation.

## Hierarchy

- **Parent:** [[FR-029-check-the-code-against-architecture-rules]]
- **Children:** none.
