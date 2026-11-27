---
id: ISS-009
type: issue
title: Results can differ between machines
status: open
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: ["[[FR-010-choose-the-toolchain-in-a-fixed-order]]", "[[FR-011-record-the-provenance-of-every-language-and-module]]"]
tags: [issue]
severity: minor
kind: risk
category: architecture
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: null
---

# Results can differ between machines

**What's unresolved.** With supported version ranges instead of one pinned version, two machines can analyse the same code with different toolchains.

**Affects.** [[FR-010-choose-the-toolchain-in-a-fixed-order]], [[FR-011-record-the-provenance-of-every-language-and-module]].

**Context / evidence.** Consequence named in [[SRC-003-adr-0001-architecture-for-language-support]].

**Options.**
1. Accept it and make it visible through provenance ([[FR-011-record-the-provenance-of-every-language-and-module]]), as decided.
2. Pin per project in the architecture file.

**Resolution.**
