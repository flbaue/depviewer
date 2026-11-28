---
id: SOL-004
type: solution-strategy
title: Architecture as code with a ratchet
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]", "[[SRC-001-vision-and-goal-of-depviewer]]"]
related: []
tags: [solution-strategy]
drivers: ["[[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]", "[[GOAL-003-ai-agents-check-and-explain-their-own-changes]]", "[[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]]"]
decisions: []
state: planned
superseded-by:
---

# Architecture as code with a ratchet

> follows [[arc42]] (section 4)

## Strategy


- **Write the architecture once, next to the code** ([[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]], [[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]]) → a committed JSON [[DT-014-architecture-file]] that extends today's exported views with selectors and rules ([[FR-025-declare-the-architecture-in-a-file]], [[FR-029-check-the-code-against-architecture-rules]]).
- **Adoptable on legacy code** ([[FR-039-accept-existing-violations-in-a-baseline]]) → existing violations go into a committed [[DT-017-baseline]] of fingerprints without line numbers. Only new violations fail, and resolved ones show so the baseline can shrink.
- **Usable by machines** ([[GOAL-003-ai-agents-check-and-explain-their-own-changes]]) → exit codes and versioned JSON output ([[FR-036-provide-the-check-command-and-its-exit-codes]], [[FR-038-report-violations-as-versioned-json]]).
