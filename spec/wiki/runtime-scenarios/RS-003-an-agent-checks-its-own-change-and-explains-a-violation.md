---
id: RS-003
type: runtime-scenario
title: An agent checks its own change and explains a violation
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [runtime-scenario]
steps:
  - {from: "[[STK-003-ai-coding-agents]]", to: "[[BB-001-depviewer-wrapper-script]]", message: depviewer check --format json --baseline baseline.json .}
  - {from: "[[BB-001-depviewer-wrapper-script]]", to: "[[BB-002-scanner-core]]", message: run DepScan check}
  - {from: "[[BB-003-cli-and-orchestration]]", to: "[[BB-012-architecture-checker]]", message: "check, as in RS-002"}
  - {from: "[[STK-003-ai-coding-agents]]", to: "[[BB-001-depviewer-wrapper-script]]", message: "depviewer path <from> <to> --model deps.json"}
  - {from: "[[BB-003-cli-and-orchestration]]", to: "[[BB-014-query-commands]]", message: find the dependency path}
  - {from: "[[BB-014-query-commands]]", to: "[[BB-004-model-and-json-writer]]", message: walk the edges}
realizes: ["[[FR-045-query-dependents-dependencies-paths-and-cycles]]", "[[FR-038-report-violations-as-versioned-json]]", "[[FR-046-document-depviewer-for-ai-agents]]"]
decisions: []
state: planned
superseded-by:
---

# An agent checks its own change and explains a violation

> follows [[arc42]] (section 6)

## Notes

The agent reads `fingerprint` and `baselined` to see which violations its change added, then asks for the path to explain it ([[GOAL-003-ai-agents-check-and-explain-their-own-changes]]).
