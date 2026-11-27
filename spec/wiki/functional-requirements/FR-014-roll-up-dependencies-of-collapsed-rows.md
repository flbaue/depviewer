---
id: FR-014
type: functional-requirement
title: Roll up dependencies of collapsed rows
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-010-viewer-reference]]", "[[SRC-015-spec-viewer-arc-geometry]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-013-explore-the-dependency-structure]]"]
building-blocks: ["[[BB-013-viewer]]"]
priority: Must
release: delivered
lane: backbone
order: 1
goal: []
delivery:
delivery-checked:
---

# Roll up dependencies of collapsed rows

## Rationale

The same code must be readable at package, file, type or member level.

## Requirement

Arcs connect the visible rows. A collapsed row carries the dependencies of everything inside it. Downward arcs are drawn on the left, upward on the right, thicker for more references, and never over the labels ([[QR-002-arcs-never-cover-the-labels]]). *Show: Packages · Files · Types · Members* expands the whole tree to that level.

## Acceptance criteria

- [ ] Collapsing `com.example.domain` draws one arc from it for all dependencies of its classes on `service`.

## Hierarchy

- **Parent:** [[FR-013-explore-the-dependency-structure]]
- **Children:** none.
