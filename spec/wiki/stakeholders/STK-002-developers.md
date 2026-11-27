---
id: STK-002
type: stakeholder
title: Developers
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: ["[[CTX-001-depviewer-context]]"]
tags: [stakeholder]
aliases:
role: developer
influence: medium
interest: high
provides: [scan and check commands]
receives: [violations with file and line]
nature: person
context_role: Developer
---

# Developers

**Snapshot.** Changes code day to day. Wants to know whether a change breaks the architecture
before review, not after.

**Goals.**
- Run one command locally and get every violation with `file:line` ([[FR-035-run-the-check-from-the-command-line-and-in-ci]]).
- Be held only to "do not make it worse" on legacy code ([[FR-039-accept-existing-violations-in-a-baseline]]).

**Concerns / pains.**
- A check that takes much longer than a build gets skipped.
- Old violations nobody can fix right now block the build.

**Interactions.** [[FR-001-scan-a-code-base-into-one-model]], [[FR-035-run-the-check-from-the-command-line-and-in-ci]], [[FR-013-explore-the-dependency-structure]].
