---
id: BB-007
type: building-block
title: Java scanner
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-009-supported-languages]]"]
related: ["[[ISS-008-moving-java-and-kotlin-into-the-jvm-extractor-can-change-the-output]]"]
tags: [building-block]
parent: "[[BB-002-scanner-core]]"
drivers: ["[[FR-003-analyse-java-with-the-jdk-compiler]]", "[[FR-008-resolve-library-types-from-the-classpath]]", "[[QR-004-no-dependency-is-invented]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
uses: ["[[BB-004-model-and-json-writer]]", "[[EIF-002-jdk]]"]
location: src/depviewer/JavaScanner.java
state: current
superseded-by:
---

# Java scanner

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

`JavaScanner`: parses and attributes Java sources with the javac Trees API inside the scanner process and adds the exact dependencies to the model. Moves into [[BB-011-jvm-extractor]] ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]).

## Interfaces

- **Provides:** scan a set of Java files with a classpath.
- **Needs:** [[EIF-002-jdk]], [[BB-004-model-and-json-writer]].
