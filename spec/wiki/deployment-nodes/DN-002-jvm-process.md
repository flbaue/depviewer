---
id: DN-002
type: deployment-node
title: JVM process
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: []
tags: [deployment-node]
stereotype: container
parent: "[[DN-001-developer-machine]]"
hosts: ["[[BB-002-scanner-core]]", "[[BB-011-jvm-extractor]]"]
connects:
  - {to: "[[DN-003-node-js-process]]", protocol: "child process, JSON on stdout"}
  - {to: "[[DN-004-dart-process]]", protocol: "child process, JSON on stdout"}
  - {to: "[[EIF-006-package-registries]]", protocol: HTTPS, note: "npm ci, dart pub get on first use"}
technology: JDK 17+
drivers: ["[[CON-001-the-scanner-core-needs-only-a-jdk-17]]"]
decisions: ["[[DR-002-the-scanner-core-is-a-static-go-binary]]"]
state: current
superseded-by:
---

# JVM process

> follows [[arc42]] (section 7)

## Purpose

Runs the scanner core, and in a second process the [[BB-011-jvm-extractor]], which the core starts. Target: runs only the [[BB-011-jvm-extractor]], started by [[DN-007-depviewer-process]].
