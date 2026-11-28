---
id: FR-022
type: functional-requirement
title: Create views and nested groups
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-010-viewer-reference]]", "[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-021-draw-the-architecture-as-views-and-groups]]"]
building-blocks: ["[[BB-013-viewer]]"]
priority: Must
release: delivered
lane: backbone
order: 1
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Create views and nested groups

## Rationale

Layers often have sublayers, such as Service and Domain inside Core.

## Requirement

*+ Group* creates a group, and a view if none is selected. *+ Sub* creates a nested group, as does dragging a group onto another or picking a parent in its details. The *⋯* menu creates, duplicates, renames, deletes, exports and imports views. *Only groups* hides unassigned code.

## Acceptance criteria

- [ ] A hand-edited file with a parent loop loads, and both groups become top-level groups.

## Hierarchy

- **Parent:** [[FR-021-draw-the-architecture-as-views-and-groups]]
- **Children:** none.
