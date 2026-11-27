---
id: DN-006
type: deployment-node
title: CI runner
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [deployment-node]
stereotype: environment
parent:
hosts: ["[[BB-001-depviewer-wrapper-script]]", "[[BB-002-scanner-core]]", "[[BB-009-typescript-extractor]]", "[[BB-010-dart-extractor]]"]
connects:
  - {to: "[[EIF-005-ci-pipeline]]", protocol: "exit code, reports"}
technology: "depviewer's own CI: GitHub Actions ubuntu-latest with Temurin 17, Node 22, Dart 3.12.0 and Chrome"
drivers: []
decisions: []
state: current
superseded-by:
---

# CI runner

> follows [[arc42]] (section 7)

## Purpose

Runs `scripts/check.sh` for depviewer itself, and `depviewer check` for teams that use it.
