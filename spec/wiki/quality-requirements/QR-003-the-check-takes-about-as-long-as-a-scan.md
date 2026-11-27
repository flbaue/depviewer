---
id: QR-003
type: quality-requirement
title: The check takes about as long as a scan
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: ["[[ISS-011-how-long-may-the-check-take]]"]
tags: [quality-requirement]
dimensions: [efficient]
priority: should
testable: false
---

# The check takes about as long as a scan

**Q42 dimensions.** Efficient.

A team only keeps a CI check that does not slow the build down much ([[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]).

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** [[EIF-005-ci-pipeline]].
- **Stimulus.** Runs `depviewer check` on a real project.
- **Environment.** A project with tens of thousands of dependencies.
- **Response.** The check reports all violations.
- **Measure.** About the duration of a scan of the same project. Not a number yet.

**Applies to.** [[FR-035-run-the-check-from-the-command-line-and-in-ci]].
**Tensions.** None known.

> [!note] Open points
> Needs a measurable threshold, see [[ISS-011-how-long-may-the-check-take]].
