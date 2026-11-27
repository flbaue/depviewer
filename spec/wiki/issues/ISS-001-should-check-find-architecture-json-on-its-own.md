---
id: ISS-001
type: issue
title: "Should check find architecture.json on its own?"
status: open
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[BB-012-architecture-checker]]", "[[FR-026-read-the-architecture-file]]", "[[FR-036-provide-the-check-command-and-its-exit-codes]]"]
tags: [issue]
severity: minor
kind: ambiguity
category: requirements
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: null
---

# Should check find architecture.json on its own?

**What's unresolved.** The spec names `architecture.json` in the project root as default, but the `check` synopsis always takes `--rules`.

**Affects.** [[FR-026-read-the-architecture-file]], [[FR-036-provide-the-check-command-and-its-exit-codes]].

**Context / evidence.** Open question in [[SRC-002-spec-architecture-file-and-rule-checks]].

**Options.**
1. Find `architecture.json` in the first project directory when `--rules` is missing. Less typing for developers and agents.
2. Always require `--rules`. Explicit, no surprise when several projects are scanned.

**Resolution.**
