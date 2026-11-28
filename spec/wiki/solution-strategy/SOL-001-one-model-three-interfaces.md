---
id: SOL-001
type: solution-strategy
title: "One model, three interfaces"
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [solution-strategy]
drivers: ["[[GOAL-004-one-answer-in-every-interface]]", "[[CON-005-selectors-are-resolved-only-in-the-scanner-core]]", "[[CON-007-checking-a-saved-model-needs-no-language-toolchain]]"]
decisions: []
state: current
superseded-by:
---

# One model, three interfaces

> follows [[arc42]] (section 4)

## Strategy


- **Same answer everywhere** ([[GOAL-004-one-answer-in-every-interface]]) → the scanner writes one [[DT-001-model]] whose format is the contract for every reader ([[CC-001-the-model-as-the-contract-between-scanner-and-viewer]]). The viewer, `depviewer check` ([[BB-012-architecture-checker]]) and later the query commands ([[BB-014-query-commands]]) and the MCP server ([[BB-015-mcp-server]]) read that model and the same architecture file.
- **One place for logic that could diverge** ([[CON-005-selectors-are-resolved-only-in-the-scanner-core]]) → selectors and rules are evaluated only in the scanner core. The viewer receives resolved assignments and violations and draws them.
- **Check without rescanning** ([[CON-007-checking-a-saved-model-needs-no-language-toolchain]]) → `check --model` reads a saved model, so CI and agents can reuse one scan.
