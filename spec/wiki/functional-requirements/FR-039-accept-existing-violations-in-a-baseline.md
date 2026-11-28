---
id: FR-039
type: functional-requirement
title: Accept existing violations in a baseline
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-035-run-the-check-from-the-command-line-and-in-ci]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 4
goal: []
delivery:
delivery-checked:
---

# Accept existing violations in a baseline

## Rationale

Legacy code must be adoptable: hold the team to "do not make it worse" and ratchet down from there.

## Requirement

`--update-baseline` writes the fingerprints of all current violations, sorted, as one JSON array meant to be committed, and exits 0, or 2 on invalid input. With `--baseline`, violations in it are reported as `baselined` and never fail. Baseline entries that no longer occur are reported as `resolved`.

## Acceptance criteria

- [ ] After `--update-baseline` the same check exits 0 with every violation `baselined`.
- [ ] Removing the offending reference from a copy of the fixture reports it as `resolved`.

## Hierarchy

- **Parent:** [[FR-035-run-the-check-from-the-command-line-and-in-ci]]
- **Children:** none.
