---
id: ISS-005
type: issue
title: "Is the extractor request sent on stdin or in a file?"
status: open
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: ["[[BB-006-toolchain-runner]]", "[[CC-002-extractor-contract]]", "[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
tags: [issue]
severity: minor
kind: question
category: architecture
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: null
---

# Is the extractor request sent on stdin or in a file?

**What's unresolved.** The contract needs one way to pass the JSON request to an extractor.

**Affects.** [[CC-002-extractor-contract]], [[BB-006-toolchain-runner]].

**Context / evidence.** Open question in [[SRC-003-adr-0001-architecture-for-language-support]]. Windows command-line limits favour a file.

**Options.**
1. stdin. No temporary file.
2. A request file. Works the same on every platform and is easy to replay.

**Resolution.**
