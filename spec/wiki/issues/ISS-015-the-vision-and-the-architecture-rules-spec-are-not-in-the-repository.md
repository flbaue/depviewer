---
id: ISS-015
type: issue
title: The vision and the architecture-rules spec are not in the repository
status: open
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[FR-025-declare-the-architecture-in-a-file]]", "[[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]]", "[[ISS-014-does-this-wiki-replace-docs-specs-and-docs-adr]]"]
tags: [issue]
severity: minor
kind: gap
category: content
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: null
---

# The vision and the architecture-rules spec are not in the repository

**What's unresolved.** Both documents exist only in the claude.ai project. The spec links `../vision.md` and AGENTS.md as if they were in `docs/`.

**Affects.** [[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]], [[FR-025-declare-the-architecture-in-a-file]], [[SRC-001-vision-and-goal-of-depviewer]], [[SRC-002-spec-architecture-file-and-rule-checks]].

**Context / evidence.** Their source pages record no file and no hash.

**Options.**
1. Commit them to `docs/vision.md` and `docs/specs/architecture-rules.md`.
2. Keep them only here, if [[ISS-014-does-this-wiki-replace-docs-specs-and-docs-adr]] makes the wiki the spec.

**Resolution.**
