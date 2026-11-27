---
id: ISS-009
type: issue
title: Results can differ between machines
status: resolved
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
resolved: 2026-11-27
---

# Results can differ between machines

**What's unresolved.** With supported version ranges instead of one pinned version, two machines can analyse the same code with different toolchains.

**Affects.** [[FR-010-choose-the-toolchain-in-a-fixed-order]], [[FR-011-record-the-provenance-of-every-language-and-module]].

**Context / evidence.** Consequence named in [[SRC-003-adr-0001-architecture-for-language-support]].

**Options.**
1. Accept it and make it visible through provenance ([[FR-011-record-the-provenance-of-every-language-and-module]]), as decided.
2. Pin per project in the architecture file.

**Resolution.** Option 1, decided by the maintainer on 2026-11-27: accepted. Provenance shows which toolchain each machine used, and `DEPVIEWER_<TOOL>` pins one in CI. Applied in [[FR-010-choose-the-toolchain-in-a-fixed-order]].
