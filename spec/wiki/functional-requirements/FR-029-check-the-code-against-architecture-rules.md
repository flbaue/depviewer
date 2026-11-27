---
id: FR-029
type: functional-requirement
title: Check the code against architecture rules
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[ISS-003-how-does-ungrouped-code-relate-to-a-layers-rule]]", "[[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]]"]
tags: [functional-requirement]
notation: prose
level: feature
parent: []
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 5
goal: ["[[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]"]
delivery:
delivery-checked:
---

# Check the code against architecture rules

## Rationale

Upward arcs are a hint. A team needs explicit rules that say what is a violation ([[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]).

## Requirement

Each view may list [[DT-015-rule|rules]] with an `id`, a `kind`, a `severity` (`error` or `warning`), a `description`, `tests` and `edgeKinds`. A group in a rule stands for itself and its subgroups, and dependencies inside one group never violate a rule between groups. Ungrouped code is only constrained by `allow-only`, `no-ungrouped` and `no-cycles`.

## Hierarchy

- **Parent:** none.
- **Children:** see `parent:` backlinks.
