---
id: ISS-003
type: issue
title: "How does ungrouped code relate to a layers rule?"
status: open
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[FR-029-check-the-code-against-architecture-rules]]", "[[FR-030-enforce-a-layer-order]]"]
tags: [issue]
severity: major
kind: ambiguity
category: requirements
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: null
---

# How does ungrouped code relate to a layers rule?

**What's unresolved.** It is open whether ungrouped code may depend on every layer and every layer on it.

**Affects.** [[FR-030-enforce-a-layer-order]], [[FR-029-check-the-code-against-architecture-rules]].

**Context / evidence.** Open question in [[SRC-002-spec-architecture-file-and-rule-checks]]. The same spec says ungrouped code is only constrained by `allow-only`, `no-ungrouped` and `no-cycles`, which suggests layers ignore it.

**Options.**
1. Layers ignore ungrouped code in both directions, as the rule table implies.
2. Ungrouped code counts as a bottom layer.
3. A field on the rule decides.

**Resolution.**
