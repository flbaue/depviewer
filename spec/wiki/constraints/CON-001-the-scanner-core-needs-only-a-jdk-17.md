---
id: CON-001
type: constraint
title: The scanner core needs only a JDK 17
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: ["[[CON-013-the-scanner-core-is-go-with-the-standard-library-only]]", "[[DR-002-the-scanner-core-is-a-static-go-binary]]"]
tags: [constraint]
category: technical
---

# The scanner core needs only a JDK 17

**Constraint.** The scanner core is Java 17, standard library only, with no build tool. The `depviewer` wrapper compiles it with javac on first use. `javac -Xlint:all` stays warning-free.

**Rationale / origin.** Zero setup for the core is a principle of the [[GOAL-001-architecture-as-code-checked-against-the-real-dependencies|vision]], set by the [[STK-004-maintainer|maintainer]].

**Impact.** No JSON, YAML or parser libraries, so the core has its own small JSON reader ([[BB-005-json-reader]]) and the architecture file is JSON ([[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]]). Kotlin is parsed by name without a Kotlin compiler until [[FR-052-analyse-kotlin-with-the-kotlin-compiler]].

**Replaced by** [[CON-013-the-scanner-core-is-go-with-the-standard-library-only]] once the Go core of [[DR-002-the-scanner-core-is-a-static-go-binary]] is delivered.
