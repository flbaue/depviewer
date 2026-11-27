---
id: DN-001
type: deployment-node
title: Developer machine
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-005-readme-of-depviewer]]", "[[SRC-009-supported-languages]]"]
related: []
tags: [deployment-node]
stereotype: environment
parent:
hosts: ["[[BB-001-depviewer-wrapper-script]]"]
connects:
  - {to: "[[DN-005-web-browser]]", protocol: "file://", note: opens the written HTML}
technology: "JDK 17+, optional Node.js 18+ and Dart SDK 3.x"
drivers: ["[[QR-005-java-projects-scan-with-nothing-but-a-jdk]]"]
decisions: []
state: current
superseded-by:
---

# Developer machine

> follows [[arc42]] (section 7)

## Purpose

Where users scan and check. macOS, Linux or Windows with a JDK, optionally Node and Dart.
