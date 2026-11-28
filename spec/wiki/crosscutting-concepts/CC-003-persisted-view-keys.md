---
id: CC-003
type: crosscutting-concept
title: Persisted view keys
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [crosscutting-concept]
category: domain
applies-to: ["[[BB-013-viewer]]", "[[BB-012-architecture-checker]]"]
drivers: ["[[CON-006-persisted-view-keys-keep-their-meaning]]"]
decisions: []
state: current
superseded-by:
---

# Persisted view keys

> follows [[arc42]] (section 8)

## Concept


Elements are assigned to groups by key, never by model index: `R:<module>`, `P:<module>:<package>`, `F:<module>:<path>`, `X`, `XP:<package>`, `XT:<qualified type>`, planned `T:<module>:<qualified name>`.

- Keys are built in one place each in the viewer and, planned, in the core for the architecture file.
- A change to how a key is built needs a migration in `loadViews()` ([[CON-006-persisted-view-keys-keep-their-meaning]]).
- `""` as a group means explicitly ungrouped. A missing parent or a parent loop degrades to a top-level group, `inGroup()` guards the walks.
