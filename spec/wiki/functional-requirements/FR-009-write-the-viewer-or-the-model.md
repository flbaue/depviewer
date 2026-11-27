---
id: FR-009
type: functional-requirement
title: Write the viewer or the model
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-008-usage-reference]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-003-cli-and-orchestration]]", "[[BB-004-model-and-json-writer]]"]
priority: Must
release: delivered
lane: backbone
order: 8
goal: []
delivery:
delivery-checked:
---

# Write the viewer or the model

## Rationale

The result must be shareable without setup ([[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]]) and readable by tools.

## Requirement

`-o` writes `.html` (default `deps.html`): the viewer with the model embedded, plus views with `--views`. A `.json` name writes the model only. `--title` sets the title, `--no-tests` leaves out test sources, `--template` picks another viewer template.

## Acceptance criteria

- [ ] `./depviewer -o out.html test/fixture` writes one file that opens offline from `file://`.
- [ ] `./depviewer -o out.json test/fixture` writes the model in the format of [[CC-001-the-model-as-the-contract-between-scanner-and-viewer]].

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.
