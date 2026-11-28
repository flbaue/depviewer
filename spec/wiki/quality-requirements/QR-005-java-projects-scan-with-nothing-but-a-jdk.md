---
id: QR-005
type: quality-requirement
title: Java projects scan with nothing but a JDK
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-005-readme-of-depviewer]]", "[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: ["[[QR-009-a-project-scans-with-the-depviewer-binary-and-its-own-toolchain]]"]
tags: [quality-requirement]
dimensions: [operable, usable]
priority: must
testable: true
---

# Java projects scan with nothing but a JDK

**Q42 dimensions.** Operable, usable.

Trying depviewer on a Java project takes one clone and one command.

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** A [[STK-002-developers|developer]] new to depviewer.
- **Stimulus.** Clones the repository and runs `./depviewer -o out.html <project>`.
- **Environment.** A machine with JDK 17 and no Node, Dart, network or account.
- **Response.** The scan produces the HTML viewer, which opens offline from `file://`.
- **Measure.** No install step besides the JDK, no network access, no account.

**Applies to.** [[FR-001-scan-a-code-base-into-one-model]], [[FR-009-write-the-viewer-or-the-model]].
**Tensions.** Exactness for languages without a JDK toolchain ([[FR-052-analyse-kotlin-with-the-kotlin-compiler]]).

**Replaced by** [[QR-009-a-project-scans-with-the-depviewer-binary-and-its-own-toolchain]] once the Go core of [[DR-002-the-scanner-core-is-a-static-go-binary]] is delivered.
