---
id: CON-011
type: constraint
title: Scanned data of other projects is never committed
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-008-usage-reference]]"]
related: []
tags: [constraint]
category: organizational
---

# Scanned data of other projects is never committed

**Constraint.** Generated output (`build/`, `deps.html`, scan JSON) and data scanned from projects you do not own are not committed.

**Rationale / origin.** A model contains the class and method names of the scanned code, so it is shared like source code.

**Impact.** Fixtures in `test/` are the only scanned code in the repository.
