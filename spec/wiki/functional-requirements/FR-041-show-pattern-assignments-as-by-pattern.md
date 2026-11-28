---
id: FR-041
type: functional-requirement
title: Show pattern assignments as by pattern
status: review
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-040-show-rule-violations-in-the-viewer]]"]
building-blocks: ["[[BB-013-viewer]]", "[[BB-003-cli-and-orchestration]]"]
priority: Should
release: m1-rules-and-check
lane: backbone
order: 1
goal: []
delivery: implemented
delivery-checked: "2026-11-28T14:07:25Z"
---

# Show pattern assignments as by pattern

## Rationale

The viewer does not match patterns itself ([[CON-005-selectors-are-resolved-only-in-the-scanner-core]]), but users must see why an element is in a group.

## Requirement

The scanner resolves all `match` selectors and embeds them per view next to `assign`, which keeps its format: `matched` holds each element's first group whose pattern selects it directly, and `excluded` the groups its `!` selectors keep it out of. The viewer decides each element's group in the order of [[FR-028-decide-each-elements-group-by-precedence]], so it shows the same groups as `check`, and labels a group that comes from a pattern "by pattern". A type selected on its own shows with its file. Dragging one, also to ungrouped, creates a manual assignment that overrides the pattern, labelled "manual (overrides pattern)". Removing that assignment brings back the pattern's group.

## Acceptance criteria

- [ ] `OrderRepository` shows in Domain labelled "by pattern". Dragged to ungrouped, it shows as "manual (overrides pattern)", and removing that assignment shows it in Domain again.

## Hierarchy

- **Parent:** [[FR-040-show-rule-violations-in-the-viewer]]
- **Children:** none.
