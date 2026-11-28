---
id: CON-005
type: constraint
title: Selectors are resolved only in the scanner core
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [constraint]
category: technical
---

# Selectors are resolved only in the scanner core

**Constraint.** Patterns (`match` selectors) are evaluated in one place, the scanner core. The viewer never matches patterns, it receives resolved assignments.

**Rationale / origin.** One implementation of matching keeps viewer and CLI from disagreeing ([[GOAL-004-one-answer-in-every-interface]]).

**Impact.** The scanner embeds the resolved pattern assignments ([[FR-041-show-pattern-assignments-as-by-pattern]]). Editing patterns in the viewer is out of scope.
