---
id: FR-040
type: functional-requirement
title: Show rule violations in the viewer
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: feature
parent: []
building-blocks: ["[[BB-013-viewer]]", "[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 7
goal: ["[[GOAL-004-one-answer-in-every-interface]]"]
delivery:
delivery-checked:
---

# Show rule violations in the viewer

## Rationale

The architect should see the same violations CI reports ([[GOAL-004-one-answer-in-every-interface]]).

## Requirement

When a view comes from an architecture file, the viewer shows pattern assignments and the violations the core found, and writes rules and patterns back unchanged on export.

## Hierarchy

- **Parent:** none.
- **Children:** see `parent:` backlinks.
