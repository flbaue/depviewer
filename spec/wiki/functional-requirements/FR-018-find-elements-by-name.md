---
id: FR-018
type: functional-requirement
title: Find elements by name
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
order: 5
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Find elements by name

## Rationale

Large trees need search.

## Requirement

*Find…* highlights matching rows. Enter and Shift+Enter jump to the next and previous match and expand the tree as needed. Esc clears the selection.

## Acceptance criteria

- [ ] Typing `Repository` and Enter expands the tree to the first row whose name matches and scrolls to it: `InMemoryOrderRepository.java` when the fixture is open at Packages.

## Hierarchy

- **Parent:** [[FR-013-explore-the-dependency-structure]]
- **Children:** none.
