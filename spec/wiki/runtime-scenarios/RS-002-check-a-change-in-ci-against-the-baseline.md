---
id: RS-002
type: runtime-scenario
title: Check a change in CI against the baseline
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [runtime-scenario]
steps:
  - {from: "[[EIF-005-ci-pipeline]]", to: "[[BB-001-depviewer-wrapper-script]]", message: depviewer check --rules architecture.json --baseline baseline.json .}
  - {from: "[[BB-001-depviewer-wrapper-script]]", to: "[[BB-002-scanner-core]]", message: run DepScan check}
  - {from: "[[BB-003-cli-and-orchestration]]", to: "[[EIF-001-scanned-code-base]]", message: "scan as in RS-001, or read --model"}
  - {from: "[[BB-003-cli-and-orchestration]]", to: "[[BB-012-architecture-checker]]", message: "model, architecture file, baseline"}
  - {from: "[[BB-012-architecture-checker]]", to: "[[BB-005-json-reader]]", message: read architecture file and baseline}
  - {from: "[[BB-012-architecture-checker]]", to: "[[BB-004-model-and-json-writer]]", message: "resolve selectors, evaluate rules"}
  - {from: "[[BB-003-cli-and-orchestration]]", to: "[[EIF-005-ci-pipeline]]", message: "violations as text or JSON, exit code 0, 1 or 2"}
realizes: ["[[FR-035-run-the-check-from-the-command-line-and-in-ci]]", "[[FR-039-accept-existing-violations-in-a-baseline]]"]
decisions: []
state: planned
superseded-by:
---

# Check a change in CI against the baseline

> follows [[arc42]] (section 6)

## Notes

Baselined violations are reported but never fail. Entries no longer found are reported as resolved. An invalid file or model gives exit code 2 before any rule runs.
