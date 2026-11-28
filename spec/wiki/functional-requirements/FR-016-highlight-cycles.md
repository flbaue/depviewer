---
id: FR-016
type: functional-requirement
title: Highlight cycles
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
order: 3
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Highlight cycles

## Rationale

Cycles are the most common sign of eroded structure.

## Requirement

Upward dependencies that close a [[DT-008-cycle]] are drawn red and every row in a cycle gets a red bar. Other upward dependencies are blue.

## Acceptance criteria

- [ ] The `domain`/`service` cycle of the fixture shows red arcs and red bars on both packages.

## Hierarchy

- **Parent:** [[FR-013-explore-the-dependency-structure]]
- **Children:** none.
