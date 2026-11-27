---
id: FR-047
type: functional-requirement
title: "Serve scan, query and check over MCP"
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-044-let-ai-agents-query-and-check-the-architecture]]"]
building-blocks: ["[[BB-015-mcp-server]]"]
priority: Could
release: m3-agent-interface
lane: backbone
order: 3
goal: []
delivery:
delivery-checked:
---

# Serve scan, query and check over MCP

## Rationale

Agents that speak MCP should not need to build command lines.

## Requirement

An MCP server exposes scan, query and check with the same results as the CLI.

## Acceptance criteria

- [ ] The MCP check of the fixture returns the same violations as `check --format json`.

## Hierarchy

- **Parent:** [[FR-044-let-ai-agents-query-and-check-the-architecture]]
- **Children:** none.

> [!note] Open points
> Transport, tools and packaging are not specified yet.
