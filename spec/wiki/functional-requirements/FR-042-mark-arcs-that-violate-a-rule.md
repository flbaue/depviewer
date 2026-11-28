---
id: FR-042
type: functional-requirement
title: Mark arcs that violate a rule
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-040-show-rule-violations-in-the-viewer]]"]
building-blocks: ["[[BB-013-viewer]]"]
priority: Should
release: m1-rules-and-check
lane: backbone
order: 2
goal: []
delivery:
delivery-checked:
---

# Mark arcs that violate a rule

## Rationale

A downward arc can still break a rule, such as `allow-only`.

## Requirement

An arc's details list its violations with rule, severity and exactness. An arc carrying an error-severity violation is drawn as a violation even if it points downward.

## Acceptance criteria

- [ ] The arc `Order` → `PriceCalculator` lists `layering`, error, exact.

## Hierarchy

- **Parent:** [[FR-040-show-rule-violations-in-the-viewer]]
- **Children:** none.
