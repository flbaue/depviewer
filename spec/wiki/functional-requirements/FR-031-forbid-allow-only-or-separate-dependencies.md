---
id: FR-031
type: functional-requirement
title: "Forbid, allow only or separate dependencies"
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-029-check-the-code-against-architecture-rules]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 2
goal: []
delivery:
delivery-checked:
---

# Forbid, allow only or separate dependencies

## Rationale

Purity rules ("the domain uses only itself and the JDK") and bounded contexts need more than an order.

## Requirement

`forbid` reports any dependency from `from` to `to`. `allow-only` reports any dependency from `from` to anything not in `to`, ungrouped code and externals included. `independent` reports any dependency between two of the listed groups. `from` and `to` take groups or `ext:` selectors.

## Acceptance criteria

- [ ] `domain-pure` (`allow-only` from `dom` to `dom` and `ext:java.**`) reports nothing for JDK types and one violation for any other external.

## Hierarchy

- **Parent:** [[FR-029-check-the-code-against-architecture-rules]]
- **Children:** none.
