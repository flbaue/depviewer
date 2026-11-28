---
id: STK-003
type: stakeholder
title: AI coding agents
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-004-guide-for-ai-coding-agents]]"]
related: ["[[CTX-001-depviewer-context]]"]
tags: [stakeholder]
aliases:
role: AI coding agent
influence: low
interest: high
provides: [query and check requests]
receives: [JSON results]
nature: person
context_role: AI coding agent
---

# AI coding agents

**Snapshot.** Claude Code, Codex, Copilot, Cursor and similar agents that change code on behalf of a
developer. A program, but it uses depviewer the way a developer does, through the CLI, so it is
treated as a user role.

**Goals.**
- Ask "may I depend on X from here?" before writing the code ([[FR-045-query-dependents-dependencies-paths-and-cycles]]).
- Check its own change and explain why A depends on B ([[GOAL-003-ai-agents-check-and-explain-their-own-changes]]).

**Concerns / pains.**
- Output meant for humans is hard to parse, so it needs versioned JSON ([[FR-038-report-violations-as-versioned-json]]).
- Without documentation written for agents it cannot find the right command ([[FR-046-document-depviewer-for-ai-agents]]).

**Interactions.** [[FR-035-run-the-check-from-the-command-line-and-in-ci]], [[FR-044-let-ai-agents-query-and-check-the-architecture]].
