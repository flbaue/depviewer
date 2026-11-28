---
id: FR-043
type: functional-requirement
title: Keep rules and patterns on export
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-040-show-rule-violations-in-the-viewer]]"]
building-blocks: ["[[BB-013-viewer]]"]
priority: Should
release: m1-rules-and-check
lane: backbone
order: 3
goal: []
delivery: implemented
delivery-checked: "2026-11-28T14:09:19Z"
---

# Keep rules and patterns on export

## Rationale

The viewer may edit only manual assignments, but must not lose what it cannot edit.

## Requirement

*Export views* writes `match`, `rules` and `version` back unchanged and leaves out what the scanner resolved ([[FR-041-show-pattern-assignments-as-by-pattern]]).

## Acceptance criteria

- [ ] Exporting the fixture's architecture file without changes gives the same `match` and `rules`.

## Hierarchy

- **Parent:** [[FR-040-show-rule-violations-in-the-viewer]]
- **Children:** none.
