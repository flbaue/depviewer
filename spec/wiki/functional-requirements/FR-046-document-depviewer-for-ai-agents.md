---
id: FR-046
type: functional-requirement
title: Document depviewer for AI agents
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]", "[[SRC-001-vision-and-goal-of-depviewer]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-044-let-ai-agents-query-and-check-the-architecture]]"]
building-blocks: []
priority: Should
release: m3-agent-interface
lane: backbone
order: 2
goal: []
delivery:
delivery-checked:
---

# Document depviewer for AI agents

## Rationale

An agent must succeed with only the CLI and its documentation ([[GOAL-003-ai-agents-check-and-explain-their-own-changes]]).

## Requirement

A page "Using depviewer from an AI agent" explains how to scan, check, query and read the JSON, with examples.

## Acceptance criteria

- [ ] An agent given only the CLI and that page finds the violation its change added to a copy of the fixture and names the dependency path.

## Hierarchy

- **Parent:** [[FR-044-let-ai-agents-query-and-check-the-architecture]]
- **Children:** none.
