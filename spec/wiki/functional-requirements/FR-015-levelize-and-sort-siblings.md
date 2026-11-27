---
id: FR-015
type: functional-requirement
title: Levelize and sort siblings
status: draft
created: 2026-11-27
updated: 2026-11-27
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
order: 2
goal: []
delivery:
delivery-checked:
---

# Levelize and sort siblings

## Rationale

The order of rows is how a reader sees the layering of the code.

## Requirement

*Levelize* (default) orders siblings so that each depends only on siblings below it, with separator lines between levels and siblings in a cycle on one level. *A–Z*, *Lines* and *Elements* are the other orders.

## Acceptance criteria

- [ ] The packages of the fixture's `domain`/`service` cycle share one level.

## Hierarchy

- **Parent:** [[FR-013-explore-the-dependency-structure]]
- **Children:** none.
