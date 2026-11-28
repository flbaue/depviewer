---
id: BB-012
type: building-block
title: Architecture checker
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[ISS-001-should-check-find-architecture-json-on-its-own]]"]
tags: [building-block]
parent: "[[BB-002-scanner-core]]"
drivers: ["[[FR-025-declare-the-architecture-in-a-file]]", "[[FR-029-check-the-code-against-architecture-rules]]", "[[FR-035-run-the-check-from-the-command-line-and-in-ci]]", "[[FR-040-show-rule-violations-in-the-viewer]]", "[[CON-005-selectors-are-resolved-only-in-the-scanner-core]]", "[[QR-003-the-check-takes-about-as-long-as-a-scan]]"]
decisions: []
uses: ["[[BB-004-model-and-json-writer]]", "[[BB-005-json-reader]]"]
location: src/depviewer/Architecture.java
state: current
superseded-by:
---

# Architecture checker

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

Part of the core that loads the architecture file ([[FR-026-read-the-architecture-file]], built), matches selectors against the model ([[FR-027-select-code-by-patterns]], built), decides each element's group and reports overlapping patterns ([[FR-028-decide-each-elements-group-by-precedence]], built), evaluates the rules on the model ([[FR-029-check-the-code-against-architecture-rules]], built), and applies the baseline and formats violations as text or JSON ([[FR-035-run-the-check-from-the-command-line-and-in-ci]], built). `Rules.java` checks, `Report.java` writes the reports and the resolved views for the viewer.

## Interfaces

- **Provides:** resolved assignments for the viewer ([[FR-041-show-pattern-assignments-as-by-pattern]]), violations for `check` and the viewer.
- **Needs:** [[BB-004-model-and-json-writer]], [[BB-005-json-reader]].

## Open issues

[[ISS-001-should-check-find-architecture-json-on-its-own]], [[ISS-003-how-does-ungrouped-code-relate-to-a-layers-rule]], [[ISS-004-should-name-based-violations-fail-the-build-by-default]].
