---
id: DR-002
type: decision-record
title: The scanner core is a static Go binary
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]", "[[CON-001-the-scanner-core-needs-only-a-jdk-17]]", "[[CON-013-the-scanner-core-is-go-with-the-standard-library-only]]", "[[BB-016-go-scanner-core]]", "[[QR-009-a-project-scans-with-the-depviewer-binary-and-its-own-toolchain]]"]
tags: [decision-record]
aliases: []
kind: architecture
decided: "2026-11-27"
decision-makers: [Florian Bauer]
consulted: []
informed: []
drivers: ["[[QR-009-a-project-scans-with-the-depviewer-binary-and-its-own-toolchain]]", "[[QR-006-internal-refactors-do-not-change-the-output]]", "[[STK-003-ai-coding-agents]]"]
amends: []
superseded-by: []
---

# The scanner core is a static Go binary

> follows [[MADR]]

## Context and Problem Statement

The scanner core is Java ([[CON-001-the-scanner-core-needs-only-a-jdk-17]]), so every user needs a JDK, also one who only scans a Dart or TypeScript project. The tooling depviewer needs should not depend on the tooling the scanned project needs. After the migration of [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]], the core holds no language analysis, only discovery, orchestration, the model, selectors and output. Which language should the core be written in?

## Decision Drivers

- A project scans with the depviewer binary and its own toolchain only ([[QR-009-a-project-scans-with-the-depviewer-binary-and-its-own-toolchain]]).
- Fast start, low memory, simple to build.
- Easy to read and change for [[STK-003-ai-coding-agents]].

## Considered Options

1. Keep Java.
2. Go, one static binary.
3. Rust, one static binary.
4. TypeScript on Node.
5. Python.

## Decision Outcome

Chosen option: **2**, because a Go binary needs nothing installed, Go's standard library covers JSON, processes, files and embedded files, and agents change Go code reliably.

1. The core is rewritten in Go after the jvm extractor exists (GitHub issue 13), never together with it.
2. The binary embeds `viewer.html` and a prebuilt jar of [[BB-011-jvm-extractor]], compiled for Java 17 in CI. A scan of a Java or Kotlin project runs that jar with the project's JDK.
3. The name-based Kotlin parser stays Java inside the jvm extractor.

### Consequences

- Good: Dart, TypeScript and later Go, Swift or Python users need no JDK. Checking a saved model needs only the binary ([[CON-007-checking-a-saved-model-needs-no-language-toolchain]]). The core's own JSON reader ([[BB-005-json-reader]]) is replaced by `encoding/json`.
- Bad: users download a release binary instead of cloning and running a script. Contributors need Go and a JDK. CI builds and publishes binaries for six platforms. The core is rewritten, about 1,100 lines of Java.

### Confirmation

Scans of the fixtures and of a larger Java project give identical JSON with the Java core and the Go core, except `generated` and `analysis` ([[QR-006-internal-refactors-do-not-change-the-output]]).

## Pros and Cons of the Options

- **1:** no rewrite, but every user needs a JDK.
- **3:** fastest and smallest, but slower builds and harder for agents to change in small steps. The core is not where the time goes: javac dominates a Java scan.
- **4:** same language as the viewer, but Java and Dart users then need Node.
- **5:** simple, but slower and dependent on the installed Python and its packages.
