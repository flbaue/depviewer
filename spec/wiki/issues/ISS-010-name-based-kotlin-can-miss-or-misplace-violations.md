---
id: ISS-010
type: issue
title: Name-based Kotlin can miss or misplace violations
status: open
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-012-spec-kotlin-support]]", "[[SRC-009-supported-languages]]"]
related: ["[[FR-004-analyse-kotlin-by-name]]", "[[FR-029-check-the-code-against-architecture-rules]]", "[[FR-052-analyse-kotlin-with-the-kotlin-compiler]]", "[[ISS-004-should-name-based-violations-fail-the-build-by-default]]"]
tags: [issue]
severity: major
kind: risk
category: requirements
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: null
---

# Name-based Kotlin can miss or misplace violations

**What's unresolved.** Until exact Kotlin exists, overloaded names link to the first match and inferred types are not resolved, so violations in Kotlin code can be wrong or missing.

**Affects.** [[FR-004-analyse-kotlin-by-name]], [[FR-029-check-the-code-against-architecture-rules]], [[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]].

**Context / evidence.** Known limits in [[SRC-012-spec-kotlin-support]] and [[SRC-009-supported-languages]].

**Options.**
1. Mark them `exact: false` and decide their default in [[ISS-004-should-name-based-violations-fail-the-build-by-default]].
2. Prioritise [[FR-052-analyse-kotlin-with-the-kotlin-compiler]] before the rules ship.

**Resolution.**
