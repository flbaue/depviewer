---
id: DN-002
type: deployment-node
title: JVM process
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]"]
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
decisions: []
state: current
superseded-by:
---

# JVM process

> follows [[arc42]] (section 7)

## Purpose

Runs the scanner core and all in-process language analysis.
