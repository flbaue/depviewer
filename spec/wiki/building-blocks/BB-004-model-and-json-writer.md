---
id: BB-004
type: building-block
title: Model and JSON writer
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [building-block]
parent: "[[BB-002-scanner-core]]"
drivers: ["[[FR-009-write-the-viewer-or-the-model]]", "[[GOAL-004-one-answer-in-every-interface]]"]
decisions: []
uses: []
location: src/depviewer/Model.java
state: current
superseded-by:
---

# Model and JSON writer

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

`Model`: nodes, edges, packages and externals shared by all languages, and the compact JSON writer. Its output is the contract of [[CC-001-the-model-as-the-contract-between-scanner-and-viewer]].

## Interfaces

- **Provides:** add nodes and edges, merge fragments, `js()` escaping, JSON output.
- **Needs:** nothing.

## Quality / Performance

Keeps the JSON compact: real projects have hundreds of thousands of edges, optional fields are omitted when empty.
