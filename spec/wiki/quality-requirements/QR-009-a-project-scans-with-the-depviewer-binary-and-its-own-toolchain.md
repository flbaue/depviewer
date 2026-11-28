---
id: QR-009
type: quality-requirement
title: A project scans with the depviewer binary and its own toolchain
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: ["[[QR-005-java-projects-scan-with-nothing-but-a-jdk]]", "[[DR-002-the-scanner-core-is-a-static-go-binary]]"]
tags: [quality-requirement]
dimensions: [operable, usable]
priority: must
testable: true
---

# A project scans with the depviewer binary and its own toolchain

**Q42 dimensions.** Operable, usable.

depviewer needs no toolchain of its own. A Dart project needs only the Dart SDK, a TypeScript project only Node, a Java or Kotlin project only a JDK. Replaces [[QR-005-java-projects-scan-with-nothing-but-a-jdk]] once the Go core ([[DR-002-the-scanner-core-is-a-static-go-binary]]) is delivered.

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** A [[STK-002-developers|developer]] new to depviewer.
- **Stimulus.** Downloads the depviewer binary and runs `depviewer -o out.html <project>`.
- **Environment.** A machine with only the toolchain of the project's language (for example the Dart SDK and no JDK), no account.
- **Response.** The scan produces the HTML viewer, which opens offline from `file://`.
- **Measure.** Downloading the binary is the only install step. The one exception: pinned extractor dependencies are installed once from [[EIF-006-package-registries]].

**Applies to.** [[FR-001-scan-a-code-base-into-one-model]], [[FR-009-write-the-viewer-or-the-model]].
**Tensions.** Release binaries must be built for every supported platform ([[CON-013-the-scanner-core-is-go-with-the-standard-library-only]]).
