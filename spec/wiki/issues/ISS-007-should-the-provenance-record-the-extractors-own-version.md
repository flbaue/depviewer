---
id: ISS-007
type: issue
title: "Should the provenance record the extractor's own version?"
status: open
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: ["[[DT-023-provenance]]", "[[FR-011-record-the-provenance-of-every-language-and-module]]"]
tags: [issue]
severity: minor
kind: question
category: architecture
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: null
---

# Should the provenance record the extractor's own version?

**What's unresolved.** `analysis` records the toolchain version, not the version of the extractor that used it.

**Affects.** [[FR-011-record-the-provenance-of-every-language-and-module]], [[DT-023-provenance]].

**Context / evidence.** Open question in [[SRC-003-adr-0001-architecture-for-language-support]].

**Options.**
1. Add the extractor version. Explains differences after a depviewer update.
2. Leave it out. The model's `generated` date and depviewer version suffice.

**Resolution.**
