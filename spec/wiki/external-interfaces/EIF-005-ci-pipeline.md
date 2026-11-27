---
id: EIF-005
type: external-interface
title: CI pipeline
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[CTX-001-depviewer-context]]"]
tags: [external-interface]
partner: "The team's CI system, for example GitHub Actions"
tier:
short_title: CI
flows:
  - {data: check command, direction: inbound, format: command line, trigger: every push or pull request, label: check}
  - {data: exit code and report, direction: outbound, format: "exit code, text, JSON, later SARIF", label: result}
---

# CI pipeline

**Purpose.** Fails a build on new violations, so the architecture is enforced on every change
([[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]).

**Flows.** The pipeline runs `depviewer check` with the architecture file and baseline and reads the
exit code and the text or JSON report ([[FR-036-provide-the-check-command-and-its-exit-codes]]). SARIF for code-review annotations is planned
([[FR-049-report-violations-as-sarif]]).

**Belongs to.** [[CTX-001-depviewer-context]]
