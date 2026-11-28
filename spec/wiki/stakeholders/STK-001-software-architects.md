---
id: STK-001
type: stakeholder
title: Software architects
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-005-readme-of-depviewer]]"]
related: ["[[CTX-001-depviewer-context]]"]
tags: [stakeholder]
aliases:
role: software architect
influence: medium
interest: high
provides: ["intended architecture (views, groups, rules)"]
receives: [dependency structure, violations]
nature: person
context_role: Architect
---

# Software architects

**Snapshot.** Responsible for the structure of one or more code bases. Needs to understand an
unfamiliar code base, agree a target structure with the team and see where it erodes.

**Goals.**
- Understand a code base and its cycles quickly ([[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]]).
- Write the agreed architecture down once and have it checked ([[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]).

**Concerns / pains.**
- Commercial architecture tools cost money.
- Diagrams drawn by hand drift away from the code.
- A violation reported from a guessed dependency costs trust in the whole check.

**Interactions.** [[FR-013-explore-the-dependency-structure]], [[FR-021-draw-the-architecture-as-views-and-groups]], [[FR-025-declare-the-architecture-in-a-file]], [[FR-040-show-rule-violations-in-the-viewer]].
