---
id: BB-008
type: building-block
title: Kotlin scanner
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-012-spec-kotlin-support]]"]
related: ["[[ISS-008-moving-java-and-kotlin-into-the-jvm-extractor-can-change-the-output]]"]
tags: [building-block]
parent: "[[BB-002-scanner-core]]"
drivers: ["[[FR-004-analyse-kotlin-by-name]]", "[[CON-001-the-scanner-core-needs-only-a-jdk-17]]", "[[QR-004-no-dependency-is-invented]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
uses: ["[[BB-004-model-and-json-writer]]"]
location: src/depviewer/KotlinScanner.java
state: current
superseded-by:
---

# Kotlin scanner

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

`KotlinScanner`: built-in lexer and parser for `.kt` files with name-based resolution, plus the matching of javac's errors for references from Java to Kotlin. Moves into [[BB-011-jvm-extractor]] ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]).

## Interfaces

- **Provides:** collect Kotlin declarations, resolve Kotlin references, link Java references to Kotlin.
- **Needs:** [[BB-004-model-and-json-writer]].

## Open issues

[[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]]: approximate results.
