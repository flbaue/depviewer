---
id: FR-021
type: functional-requirement
title: Draw the architecture as views and groups
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-010-viewer-reference]]"]
related: []
tags: [functional-requirement]
notation: prose
level: feature
parent: []
building-blocks: ["[[BB-013-viewer]]"]
priority: Must
release: delivered
lane: backbone
order: 3
goal: ["[[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]]", "[[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]"]
delivery:
delivery-checked:
---

# Draw the architecture as views and groups

## Rationale

A team needs to put its intended structure next to the real one to see where they differ ([[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]]).

## Requirement

Users define views, each an independent set of nested groups, and assign code to groups in the viewer. Collapsed groups are levelized like code, so every blue or red arc between groups goes against the defined order. Views can be shared as a file.

## Hierarchy

- **Parent:** none.
- **Children:** see `parent:` backlinks.

## History

> Delivered before this wiki was captured: status "done" in [[SRC-001-vision-and-goal-of-depviewer]].
