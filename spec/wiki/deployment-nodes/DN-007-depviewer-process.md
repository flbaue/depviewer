---
id: DN-007
type: deployment-node
title: depviewer process
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: []
tags: [deployment-node]
stereotype: container
parent: "[[DN-001-developer-machine]]"
hosts: ["[[BB-016-go-scanner-core]]"]
connects:
  - {to: "[[DN-002-jvm-process]]", protocol: "child process, JSON on stdout"}
  - {to: "[[DN-003-node-js-process]]", protocol: "child process, JSON on stdout"}
  - {to: "[[DN-004-dart-process]]", protocol: "child process, JSON on stdout"}
technology: static Go binary
drivers: ["[[CON-013-the-scanner-core-is-go-with-the-standard-library-only]]"]
decisions: ["[[DR-002-the-scanner-core-is-a-static-go-binary]]"]
state: target
superseded-by:
---

# depviewer process

> follows [[arc42]] (section 7)

## Purpose

The Go binary a user downloads and runs. Starts the JVM only for Java and Kotlin modules, where [[DN-002-jvm-process]] then hosts [[BB-011-jvm-extractor]] alone.
