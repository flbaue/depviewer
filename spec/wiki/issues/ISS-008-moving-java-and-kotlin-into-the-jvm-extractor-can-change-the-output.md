---
id: ISS-008
type: issue
title: Moving Java and Kotlin into the jvm extractor can change the output
status: open
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: ["[[BB-007-java-scanner]]", "[[BB-008-kotlin-scanner]]", "[[BB-011-jvm-extractor]]", "[[QR-006-internal-refactors-do-not-change-the-output]]"]
tags: [issue]
severity: major
kind: risk
category: architecture
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: null
---

# Moving Java and Kotlin into the jvm extractor can change the output

**What's unresolved.** The refactor touches the most-used path, and any difference reaches every user.

**Affects.** [[BB-011-jvm-extractor]], [[BB-007-java-scanner]], [[BB-008-kotlin-scanner]], [[QR-006-internal-refactors-do-not-change-the-output]].

**Context / evidence.** Consequence named in [[SRC-003-adr-0001-architecture-for-language-support]].

**Options.**
1. Mitigation chosen in [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]: identical JSON on the fixtures and a large real project before the old path is removed.

**Resolution.**
