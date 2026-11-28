---
id: BB-003
type: building-block
title: CLI and orchestration
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-011-spec-multi-language-architecture]]"]
related: []
tags: [building-block]
parent: "[[BB-002-scanner-core]]"
drivers: ["[[FR-002-find-modules-by-their-build-markers]]", "[[FR-009-write-the-viewer-or-the-model]]", "[[FR-036-provide-the-check-command-and-its-exit-codes]]"]
decisions: []
uses: ["[[BB-004-model-and-json-writer]]", "[[BB-006-toolchain-runner]]", "[[BB-007-java-scanner]]", "[[BB-008-kotlin-scanner]]", "[[BB-012-architecture-checker]]", "[[BB-014-query-commands]]", "[[BB-013-viewer]]", "[[EIF-001-scanned-code-base]]"]
location: src/depviewer/DepScan.java
state: current
superseded-by:
---

# CLI and orchestration

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

`DepScan`: parses options, discovers modules by their markers, decides which analysis runs for which files, calls it and writes the output. Will also host `check` and the query commands.

## Interfaces

- **Provides:** `main`, options of [[FR-009-write-the-viewer-or-the-model]], later [[FR-036-provide-the-check-command-and-its-exit-codes]].
- **Needs:** [[BB-004-model-and-json-writer]], [[BB-006-toolchain-runner]], [[BB-007-java-scanner]], [[BB-008-kotlin-scanner]], later [[BB-012-architecture-checker]] and [[BB-014-query-commands]]. Reads the code base ([[EIF-001-scanned-code-base]]) and the template ([[BB-013-viewer]]).
