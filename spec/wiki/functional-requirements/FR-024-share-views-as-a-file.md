---
id: FR-024
type: functional-requirement
title: Share views as a file
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-034-quick-start-guide]]", "[[SRC-010-viewer-reference]]", "[[SRC-008-usage-reference]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-021-draw-the-architecture-as-views-and-groups]]"]
building-blocks: ["[[BB-013-viewer]]", "[[BB-003-cli-and-orchestration]]"]
priority: Must
release: delivered
lane: backbone
order: 3
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Share views as a file

## Rationale

One agreed architecture must be visible to the whole team, not only in one browser.

## Requirement

Views are stored in the browser per project title. *⋯ › Export views* writes them as JSON in the format of [[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]], which can be imported again or embedded with `depviewer --views views.json …`.

## Acceptance criteria

- [ ] `test/fixture-views.json` embedded with `--views` shows the Layers view on first open in a fresh browser.

## Hierarchy

- **Parent:** [[FR-021-draw-the-architecture-as-views-and-groups]]
- **Children:** none.
