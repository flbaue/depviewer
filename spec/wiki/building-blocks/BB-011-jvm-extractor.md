---
id: BB-011
type: building-block
title: jvm extractor
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-029-github-issue-13-move-java-and-kotlin-into-a-jvm-extractor-behind-the-contract]]"]
related: ["[[ISS-006-how-is-the-projects-kotlin-version-read-without-running-gradle]]", "[[ISS-008-moving-java-and-kotlin-into-the-jvm-extractor-can-change-the-output]]"]
tags: [building-block]
parent: "[[CTX-001-depviewer-context]]"
drivers: ["[[FR-052-analyse-kotlin-with-the-kotlin-compiler]]", "[[QR-006-internal-refactors-do-not-change-the-output]]", "[[CON-001-the-scanner-core-needs-only-a-jdk-17]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
uses: ["[[EIF-002-jdk]]"]
location:
state: planned
superseded-by:
---

# jvm extractor

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

Planned extractor for Java and Kotlin, written in Java, needing only the JDK. Takes over [[BB-007-java-scanner]] and [[BB-008-kotlin-scanner]] behind the contract of [[CC-002-extractor-contract]] and keeps Java and Kotlin linking in one run. Later uses the Kotlin compiler for exact Kotlin ([[FR-052-analyse-kotlin-with-the-kotlin-compiler]]).

## Interfaces

- **Provides:** the extractor contract, `--describe`.
- **Needs:** [[EIF-002-jdk]], later the Kotlin compiler.

## Quality / Performance

Java- and Kotlin-only scans produce identical JSON before and after the move ([[QR-006-internal-refactors-do-not-change-the-output]]).

## Open issues

[[ISS-008-moving-java-and-kotlin-into-the-jvm-extractor-can-change-the-output]], [[ISS-006-how-is-the-projects-kotlin-version-read-without-running-gradle]].
