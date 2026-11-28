---
id: FR-020
type: functional-requirement
title: Open a model in the viewer
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-034-quick-start-guide]]", "[[SRC-008-usage-reference]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-013-explore-the-dependency-structure]]"]
building-blocks: ["[[BB-013-viewer]]"]
priority: Should
release: delivered
lane: backbone
order: 7
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Open a model in the viewer

## Rationale

A model written as JSON in CI should open without rescanning.

## Requirement

`viewer.html` opens a `.json` model with *Open…*, by drag and drop, or with `?data=<url>` over HTTP. `?level=1` to `4` opens the tree at Packages, Files, Types or Members.

## Acceptance criteria

- [ ] Dropping `deps.json` on `viewer.html` shows the same tree as the HTML written by the same scan.

## Hierarchy

- **Parent:** [[FR-013-explore-the-dependency-structure]]
- **Children:** none.
