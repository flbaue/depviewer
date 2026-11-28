---
id: FR-028
type: functional-requirement
title: "Decide each element's group by precedence"
status: draft
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-025-declare-the-architecture-in-a-file]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 3
goal: []
delivery:
delivery-checked:
---

# Decide each element's group by precedence

## Rationale

Patterns and manual assignments must combine in one predictable way.

## Requirement

For each element: an explicit `assign` entry for it, else a `match` that selects it, else the group of its nearest ancestor by the same two steps. An element that a group excludes with `!` never gets that group from an ancestor. It takes the next ancestor's group above, or stays ungrouped. If two groups' patterns select the same element, the group listed first wins and `check` reports an overlap warning naming both groups and the element. A pattern that overrides another group the element would get from an ancestor's pattern is an overlap too, though the pattern on the element still wins. There is no warning when the element has its own `assign` entry, or for the inherited case when one group is nested in the other. Types can be assigned on their own, with `type:` selectors or the key `T:<module>:<qualified name>`. Members always move with their type.

## Acceptance criteria

- [ ] Without `@interface`, `InMemoryOrderRepository` is selected by `dom` and `infra`: one overlap warning, and it goes to `dom`, listed first.
- [ ] A group with `package:com.example.domain.**` and `!path:**/Status.java` leaves `Status.java` and its type `Status` ungrouped.

## Hierarchy

- **Parent:** [[FR-025-declare-the-architecture-in-a-file]]
- **Children:** none.
