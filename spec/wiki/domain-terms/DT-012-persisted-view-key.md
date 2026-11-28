---
id: DT-012
type: domain-term
title: Persisted view key
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [domain-term]
aliases: [view key]
bounded-context: depviewer
agreed: false
stereotype:
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Persisted view key

**Definition.** The string that identifies an element across scans in saved views: `R:<module>`, `P:<module>:<package>`, `F:<module>:<path>`, `X`, `XP:<package>`, `XT:<qualified type>`, planned `T:<module>:<qualified name>`.

**In context.** Used in localStorage, exported views and the architecture file. Never changes meaning ([[CON-006-persisted-view-keys-keep-their-meaning]]).

**Distinguish from.** The index of a node in the model, which changes with every scan.
