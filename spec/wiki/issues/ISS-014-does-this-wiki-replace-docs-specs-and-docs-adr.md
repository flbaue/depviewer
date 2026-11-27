---
id: ISS-014
type: issue
title: "Does this wiki replace docs/specs and docs/adr?"
status: open
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]"]
related: ["[[CC-005-spec-first-then-test-driven]]", "[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]", "[[ISS-015-the-vision-and-the-architecture-rules-spec-are-not-in-the-repository]]"]
tags: [issue]
severity: major
kind: question
category: method
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: null
---

# Does this wiki replace docs/specs and docs/adr?

**What's unresolved.** AGENTS.md tells agents to start every change from `docs/specs/`. With this wiki there are two places for the same requirements.

**Affects.** [[CC-005-spec-first-then-test-driven]], every FR and [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]].

**Context / evidence.** This wiki was captured from the existing specs, ADR and docs on 2026-11-27. Nothing in the repository points to it yet.

**Options.**
1. The wiki is the spec: AGENTS.md points to `spec/`, `docs/specs/` and `docs/adr/` are retired or reduced to links.
2. Both: the wiki holds requirements and architecture, `docs/specs/` keeps detailed behaviour, each linking the other.
3. The wiki stays a read-only summary of `docs/`.

**Resolution.**
