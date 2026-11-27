---
id: EIF-002
type: external-interface
title: JDK
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-009-supported-languages]]"]
related: ["[[CTX-001-depviewer-context]]"]
tags: [external-interface]
partner: "JDK 17 or newer with javac (Trees API)"
tier:
short_title: JDK
flows:
  - {data: Java sources and classpath, direction: outbound, format: "javac API, in process", trigger: scan of a Java module, label: compile}
  - {data: attributed syntax trees, direction: inbound, format: "javac API, in process", label: trees}
---

# JDK

**Purpose.** Runs the scanner core and resolves Java references exactly with javac's Trees API.

**Flows.** The scanner passes Java sources and the classpath to javac inside its own process and
reads the parsed and attributed syntax trees back. The JDK is the one toolchain every user needs
([[CON-001-the-scanner-core-needs-only-a-jdk-17]]).

**Belongs to.** [[CTX-001-depviewer-context]]
