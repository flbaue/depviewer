---
id: FR-013
type: functional-requirement
title: Explore the dependency structure
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-010-viewer-reference]]", "[[SRC-005-readme-of-depviewer]]"]
related: []
tags: [functional-requirement]
notation: prose
level: feature
parent: []
building-blocks: ["[[BB-013-viewer]]"]
priority: Must
release: delivered
lane: backbone
order: 2
goal: ["[[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]]"]
delivery:
delivery-checked:
---

# Explore the dependency structure

## Rationale

Architects need to see where the design erodes before they can agree on a target ([[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]]).

## Requirement

The viewer shows the model as a collapsible tree (module › package › file › type › member) with every dependency between visible rows drawn as an arc, orders siblings by their dependencies, highlights cycles and explains every arc down to `file:line`.

## Hierarchy

- **Parent:** none.
- **Children:** see `parent:` backlinks.

## History

> Delivered before this wiki was captured: status "done" in [[SRC-001-vision-and-goal-of-depviewer]].
