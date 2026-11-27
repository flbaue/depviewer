---
id: CON-009
type: constraint
title: No new tooling or dependencies without the maintainer
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-006-contributing-guide]]"]
related: []
tags: [constraint]
category: organizational
---

# No new tooling or dependencies without the maintainer

**Constraint.** New build tooling or dependencies are added only after the [[STK-004-maintainer|maintainer]] agreed.

**Rationale / origin.** Keeps the project small and inspectable (AGENTS.md, CONTRIBUTING.md).

**Impact.** Extractors may use their language's official tooling with pinned versions ([[CON-010-extractors-pin-their-dependencies]]). Everything else is discussed first.
