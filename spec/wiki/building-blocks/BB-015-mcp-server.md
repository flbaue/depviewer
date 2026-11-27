---
id: BB-015
type: building-block
title: MCP server
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: []
tags: [building-block]
parent: "[[CTX-001-depviewer-context]]"
drivers: ["[[FR-047-serve-scan-query-and-check-over-mcp]]", "[[GOAL-003-ai-agents-check-and-explain-their-own-changes]]", "[[GOAL-004-one-answer-in-every-interface]]"]
decisions: []
uses: ["[[BB-002-scanner-core]]"]
location:
state: planned
superseded-by:
---

# MCP server

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

Planned server that exposes scan, query and check to agents over MCP with the same results as the CLI.

## Interfaces

- **Provides:** [[FR-047-serve-scan-query-and-check-over-mcp]].
- **Needs:** [[BB-003-cli-and-orchestration]], [[BB-012-architecture-checker]], [[BB-014-query-commands]].
