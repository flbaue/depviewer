---
id: RS-001
type: runtime-scenario
title: Scan a mixed repository into one viewer file
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-011-spec-multi-language-architecture]]", "[[SRC-012-spec-kotlin-support]]", "[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [runtime-scenario]
steps:
  - {from: "[[STK-002-developers]]", to: "[[BB-001-depviewer-wrapper-script]]", message: "./depviewer -o out.html <dirs>"}
  - {from: "[[BB-001-depviewer-wrapper-script]]", to: "[[BB-002-scanner-core]]", message: "compile if sources changed, run DepScan with --template"}
  - {from: "[[BB-003-cli-and-orchestration]]", to: "[[EIF-001-scanned-code-base]]", message: discover modules by build markers}
  - {from: "[[BB-003-cli-and-orchestration]]", to: "[[BB-006-toolchain-runner]]", message: "Java and Kotlin files, source roots and classpath"}
  - {from: "[[BB-006-toolchain-runner]]", to: "[[BB-011-jvm-extractor]]", message: one run for all Java and Kotlin modules}
  - {from: "[[BB-011-jvm-extractor]]", to: "[[BB-008-kotlin-scanner]]", message: declare Kotlin first}
  - {from: "[[BB-011-jvm-extractor]]", to: "[[BB-007-java-scanner]]", message: "Java files and classpath, javac errors for Kotlin names"}
  - {from: "[[BB-007-java-scanner]]", to: "[[EIF-002-jdk]]", message: parse and attribute}
  - {from: "[[BB-003-cli-and-orchestration]]", to: "[[BB-006-toolchain-runner]]", message: TypeScript modules and Dart packages}
  - {from: "[[BB-006-toolchain-runner]]", to: "[[BB-009-typescript-extractor]]", message: one run per toolchain for all TypeScript modules}
  - {from: "[[BB-006-toolchain-runner]]", to: "[[BB-010-dart-extractor]]", message: one run per Dart SDK for all Dart packages}
  - {from: "[[BB-006-toolchain-runner]]", to: "[[BB-004-model-and-json-writer]]", message: merge fragments}
  - {from: "[[BB-003-cli-and-orchestration]]", to: "[[BB-013-viewer]]", message: "replace /*DEPS_DATA*/ with the model"}
realizes: ["[[FR-001-scan-a-code-base-into-one-model]]", "[[FR-009-write-the-viewer-or-the-model]]"]
decisions: []
state: current
superseded-by:
---

# Scan a mixed repository into one viewer file

> follows [[arc42]] (section 6)

## Notes

Extractors answer with a fragment on stdout and diagnostics on stderr. A missing toolchain skips its step with a note ([[FR-007-skip-a-language-whose-toolchain-is-missing]]). Kotlin declarations are collected before Java is attributed, so javac's errors for Kotlin names can be matched.
