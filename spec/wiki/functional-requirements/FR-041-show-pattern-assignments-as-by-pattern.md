---
id: FR-041
type: functional-requirement
title: Show pattern assignments as by pattern
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-040-show-rule-violations-in-the-viewer]]"]
building-blocks: ["[[BB-013-viewer]]", "[[BB-003-cli-and-orchestration]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 1
goal: []
delivery:
delivery-checked:
---

# Show pattern assignments as by pattern

## Rationale

The viewer does not match patterns itself ([[CON-005-selectors-are-resolved-only-in-the-scanner-core]]), but users must see why an element is in a group.

## Requirement

The scanner resolves all `match` selectors and embeds them as assignments marked `"from": "match"`. The viewer shows them like manual ones, labelled "by pattern". They cannot be removed by drag, only overridden by a manual assignment.

## Acceptance criteria

- [ ] `OrderRepository` shows in Domain labelled "by pattern", and dragging it out of Domain is refused.

## Hierarchy

- **Parent:** [[FR-040-show-rule-violations-in-the-viewer]]
- **Children:** none.
