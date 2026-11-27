---
id: STK-004
type: stakeholder
title: Maintainer
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-005-readme-of-depviewer]]", "[[SRC-007-mit-license]]"]
related: []
tags: [stakeholder]
aliases:
role: maintainer
influence: high
interest: high
provides:
receives:
nature: person
context_role: none
---

# Maintainer

**Snapshot.** Florian Bauer, owner of github.com/flbaue/depviewer. Directs the AI coding agent that
writes most of the code, decides what gets built and reviews every change.

**Goals.**
- Every feature done spec first and test-driven ([[CC-005-spec-first-then-test-driven]]).
- The project stays small: no build tool, no dependencies, no frameworks ([[CON-001-the-scanner-core-needs-only-a-jdk-17]], [[CON-002-the-viewer-is-one-self-contained-html-file]]).

**Concerns / pains.**
- New tooling or dependencies creeping in ([[CON-009-no-new-tooling-or-dependencies-without-the-maintainer]]).
- Silent breaks of users' saved views ([[CON-006-persisted-view-keys-keep-their-meaning]]).

**Interactions.** Decides the open [[ISS-001-should-check-find-architecture-json-on-its-own|issues]] and accepts decision records such as [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]].
