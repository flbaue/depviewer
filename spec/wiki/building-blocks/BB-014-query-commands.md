---
id: BB-014
type: building-block
title: Query commands
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: []
tags: [building-block]
parent: "[[BB-002-scanner-core]]"
drivers: ["[[FR-045-query-dependents-dependencies-paths-and-cycles]]", "[[GOAL-003-ai-agents-check-and-explain-their-own-changes]]"]
decisions: []
uses: ["[[BB-004-model-and-json-writer]]"]
location:
state: planned
superseded-by:
---

# Query commands

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

Planned CLI commands `dependents`, `dependencies`, `path` and `cycles` on a scan or saved model, with JSON output for agents.

## Interfaces

- **Provides:** [[FR-045-query-dependents-dependencies-paths-and-cycles]].
- **Needs:** [[BB-004-model-and-json-writer]].
