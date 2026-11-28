---
id: FR-035
type: functional-requirement
title: Run the check from the command line and in CI
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]", "[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-022-github-issue-6-architecture-checks-for-ai-agents-rules-for-forbidden-layer-module]]"]
related: []
tags: [functional-requirement]
notation: prose
level: feature
parent: []
building-blocks: ["[[BB-012-architecture-checker]]", "[[BB-003-cli-and-orchestration]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 6
goal: ["[[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]", "[[GOAL-003-ai-agents-check-and-explain-their-own-changes]]", "[[GOAL-004-one-answer-in-every-interface]]"]
delivery:
delivery-checked:
---

# Run the check from the command line and in CI

## Rationale

Rules only matter if they fail a build ([[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]) and if agents can run them ([[GOAL-003-ai-agents-check-and-explain-their-own-changes]]).

## Requirement

`depviewer check` scans or loads a model, checks it against the architecture file, prints violations as text or versioned JSON, sets an exit code and can accept existing violations in a baseline.

## Hierarchy

- **Parent:** none.
- **Children:** see `parent:` backlinks.
