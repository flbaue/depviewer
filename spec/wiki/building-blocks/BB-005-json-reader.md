---
id: BB-005
type: building-block
title: JSON reader
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[ISS-002-does-the-json-reader-need-comment-support]]"]
tags: [building-block]
parent: "[[BB-002-scanner-core]]"
drivers: ["[[CON-001-the-scanner-core-needs-only-a-jdk-17]]", "[[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]]"]
decisions: []
uses: []
location: src/depviewer/Json.java
state: current
superseded-by:
---

# JSON reader

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

`Json`: minimal JSON reader with the standard library only. Reads extractor fragments, exported views and, planned, the architecture file and baseline.

## Interfaces

- **Provides:** parse a JSON text into maps, lists and scalars.
- **Needs:** nothing.

## Open issues

None. Comments stay unsupported ([[ISS-002-does-the-json-reader-need-comment-support]]).
