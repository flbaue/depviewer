---
id: BB-001
type: building-block
title: depviewer wrapper script
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-008-usage-reference]]", "[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: []
tags: [building-block]
parent: "[[CTX-001-depviewer-context]]"
drivers: ["[[QR-005-java-projects-scan-with-nothing-but-a-jdk]]", "[[CON-001-the-scanner-core-needs-only-a-jdk-17]]"]
decisions: []
uses: ["[[BB-002-scanner-core]]", "[[EIF-002-jdk]]"]
location: depviewer
state: current
superseded-by: "[[BB-016-go-scanner-core]]"
---

# depviewer wrapper script

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

POSIX shell script that users run. Compiles the core into `build/depviewer-classes` on first use and whenever its sources changed, passes `--template` and starts `depviewer.DepScan`.

## Interfaces

- **Provides:** `./depviewer [options] <dir>...`, later `./depviewer check …`.
- **Needs:** `javac` and `java` of a JDK 17+ ([[EIF-002-jdk]]).
