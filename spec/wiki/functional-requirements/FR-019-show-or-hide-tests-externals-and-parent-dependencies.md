---
id: FR-019
type: functional-requirement
title: "Show or hide tests, externals and parent dependencies"
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
priority: Should
release: delivered
lane: backbone
order: 6
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Show or hide tests, externals and parent dependencies

## Rationale

Different questions need different noise levels.

## Requirement

Toggles: *Tests*, *External* (JDK and library types, hiding externals nothing visible uses) and *Parent deps* (members using their own class).

## Acceptance criteria

- [ ] Turning *Tests* off removes the rows of `src/test/` and their arcs.

## Hierarchy

- **Parent:** [[FR-013-explore-the-dependency-structure]]
- **Children:** none.
