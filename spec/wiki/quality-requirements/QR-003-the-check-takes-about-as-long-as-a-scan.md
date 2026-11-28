---
id: QR-003
type: quality-requirement
title: The check takes about as long as a scan
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: ["[[ISS-011-how-long-may-the-check-take]]"]
tags: [quality-requirement]
dimensions: [efficient]
priority: should
testable: true
---

# The check takes about as long as a scan

**Q42 dimensions.** Efficient.

A team only keeps a CI check that does not slow the build down much ([[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]).

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** [[EIF-005-ci-pipeline]].
- **Stimulus.** Runs `depviewer check` on a real project.
- **Environment.** A project with at least 50,000 dependencies, scanned and checked on the same machine.
- **Response.** The check reports all violations.
- **Measure.** `check` on the project directory takes at most 1.2 times a plain scan of the same project ([[ISS-011-how-long-may-the-check-take]]).

**Applies to.** [[FR-035-run-the-check-from-the-command-line-and-in-ci]].
**Tensions.** None known.
