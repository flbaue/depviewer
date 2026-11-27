---
id: FR-038
type: functional-requirement
title: Report violations as versioned JSON
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[ISS-004-should-name-based-violations-fail-the-build-by-default]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-035-run-the-check-from-the-command-line-and-in-ci]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 3
goal: []
delivery:
delivery-checked:
---

# Report violations as versioned JSON

## Rationale

Agents and tools need a stable, parseable format ([[STK-003-ai-coding-agents]]).

## Requirement

`--format json` writes `format: 1`, the model's title, date and `analysis`, a `summary` (error, warning, baselined, resolved, overlaps) and `violations` with rule, view, severity, kind, `from` and `to` (key, qualified name, path, group), lines, count, `exact`, `baselined` and a `fingerprint` without line numbers.

## Acceptance criteria

- [ ] The fingerprint of the fixture's `layering` violation stays the same after a blank line is added above `Order.total()`.
- [ ] `exact` is false for violations from Kotlin sources until [[FR-052-analyse-kotlin-with-the-kotlin-compiler]].

## Hierarchy

- **Parent:** [[FR-035-run-the-check-from-the-command-line-and-in-ci]]
- **Children:** none.
