---
id: FR-048
type: functional-requirement
title: Review only what a change touches
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: feature
parent: []
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Should
release: unscheduled
lane: backbone
order: 9
goal: ["[[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]"]
delivery:
delivery-checked:
---

# Review only what a change touches

## Rationale

In code review only the violations a change adds are interesting, shown where the change is.

## Requirement

The check reports in SARIF for code-review annotations and can be limited to the files a change touches.

## Hierarchy

- **Parent:** none.
- **Children:** see `parent:` backlinks.
