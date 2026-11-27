---
id: CON-006
type: constraint
title: Persisted view keys keep their meaning
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [constraint]
category: technical
---

# Persisted view keys keep their meaning

**Constraint.** The keys that assign elements to groups, `R:<module>`, `P:<module>:<package>`, `F:<module>:<path>`, `X`, `XP:<package>` and `XT:<qualified type>`, never change meaning. New keys, such as `T:<module>:<qualified name>`, may be added.

**Rationale / origin.** Saved views in browsers, exported files and architecture files depend on them. A change breaks everyone's views silently.

**Impact.** Any unavoidable change needs a migration in `loadViews()` ([[CC-003-persisted-view-keys]]).
