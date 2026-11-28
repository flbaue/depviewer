---
id: FR-025
type: functional-requirement
title: Declare the architecture in a file
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]", "[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-022-github-issue-6-architecture-checks-for-ai-agents-rules-for-forbidden-layer-module]]"]
related: ["[[ISS-015-the-vision-and-the-architecture-rules-spec-are-not-in-the-repository]]"]
tags: [functional-requirement]
notation: prose
level: feature
parent: []
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 4
goal: ["[[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]", "[[GOAL-004-one-answer-in-every-interface]]"]
delivery:
delivery-checked:
---

# Declare the architecture in a file

## Rationale

Groups drawn by hand live in one browser. The architecture must be written down once, committed and read by every interface ([[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]], [[GOAL-004-one-answer-in-every-interface]]).

## Requirement

A committed [[DT-014-architecture-file]] holds views, groups defined by selectors and rules. Assignment of each element to a group follows one precedence, and overlapping patterns are reported.

## Hierarchy

- **Parent:** none.
- **Children:** see `parent:` backlinks.
