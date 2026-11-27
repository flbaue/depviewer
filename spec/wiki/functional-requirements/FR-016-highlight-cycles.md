---
id: FR-016
type: functional-requirement
title: Highlight cycles
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
order: 3
goal: []
delivery: implemented
delivery-checked: "2026-11-27T21:56:32+01:00"
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
