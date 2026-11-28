---
id: FR-044
type: functional-requirement
title: Let AI agents query and check the architecture
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]", "[[SRC-022-github-issue-6-architecture-checks-for-ai-agents-rules-for-forbidden-layer-module]]"]
related: []
tags: [functional-requirement]
notation: prose
level: feature
parent: []
building-blocks: ["[[BB-014-query-commands]]", "[[BB-015-mcp-server]]"]
priority: Should
release: m3-agent-interface
lane: backbone
order: 8
goal: ["[[GOAL-003-ai-agents-check-and-explain-their-own-changes]]"]
delivery:
delivery-checked:
---

# Let AI agents query and check the architecture

## Rationale

Agents write a growing share of code and should keep to the architecture without a human in the loop ([[GOAL-003-ai-agents-check-and-explain-their-own-changes]]).

## Requirement

Agents get query commands with JSON output, documentation written for them and later an MCP server that exposes scan, query and check.

## Hierarchy

- **Parent:** none.
- **Children:** see `parent:` backlinks.
