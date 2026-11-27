---
id: DR-001
type: decision-record
title: Every language is analysed by an extractor behind one versioned contract
status: accepted
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: ["[[CC-002-extractor-contract]]", "[[FR-010-choose-the-toolchain-in-a-fixed-order]]", "[[FR-011-record-the-provenance-of-every-language-and-module]]", "[[FR-012-link-the-modules-of-a-monorepo]]", "[[FR-051-analyse-more-languages]]", "[[ISS-005-is-the-extractor-request-sent-on-stdin-or-in-a-file]]", "[[ISS-014-does-this-wiki-replace-docs-specs-and-docs-adr]]", "[[SOL-002-each-language-is-analysed-by-its-own-toolchain]]"]
tags: [decision-record]
aliases: [ADR-0001]
kind: architecture
decided: "2026-11-26"
decision-makers: [Florian Bauer]
consulted: []
informed: []
drivers: ["[[QR-004-no-dependency-is-invented]]", "[[QR-005-java-projects-scan-with-nothing-but-a-jdk]]", "[[FR-010-choose-the-toolchain-in-a-fixed-order]]", "[[FR-011-record-the-provenance-of-every-language-and-module]]", "[[FR-012-link-the-modules-of-a-monorepo]]"]
amends: []
superseded-by: []
---

# Every language is analysed by an extractor behind one versioned contract

> follows [[MADR]]

## Context and Problem Statement

depviewer supports four languages in three ways: Java with javac and Kotlin with a built-in parser inside the scanner, TypeScript with a Node extractor run per module, Dart with an extractor run once per scan. Swift, Python, Go, C# and exact Kotlin are wanted next. Problems: two integration paths, broken links inside TypeScript monorepos (#4), no common toolchain policy, no record of how exact the model is, and no versioned or tested fragment format. How should a language be added from now on?

## Decision Drivers

- Compiler-exact dependencies, and visible exactness where they are not ([[QR-004-no-dependency-is-invented]], [[FR-011-record-the-provenance-of-every-language-and-module]]).
- Zero setup for Java ([[QR-005-java-projects-scan-with-nothing-but-a-jdk]]).
- Monorepos link correctly ([[FR-012-link-the-modules-of-a-monorepo]]).
- One way to add a language.

## Considered Options

1. Every language, Java and Kotlin included, as an extractor behind one versioned contract.
2. Keep Java and Kotlin in the process, extractors for the rest.
3. Pinned toolchains only.
4. The user's toolchains only.
5. Language servers (LSP) as the common integration.
6. Reading compiled output (`--classes`) as the JVM mechanism.

## Decision Outcome

Chosen option: **1**, because it gives one contract for everything and a language-neutral core.

1. Extractors are grouped by ecosystem: `jvm` (Java, Kotlin, written in Java, JDK only), `typescript`, `dart`, later `swift`, `python`, `go`, `dotnet`.
2. The core starts each extractor once per scan with all its modules.
3. Toolchains: override, project version, `PATH`, pinned download, else skip. Only versions in the extractor's supported range are used ([[FR-010-choose-the-toolchain-in-a-fixed-order]]).
4. Provenance per language and module, not per dependency ([[FR-011-record-the-provenance-of-every-language-and-module]]).
5. A versioned contract with a conformance test ([[CC-002-extractor-contract]]).
6. References between ecosystems are not linked, except by identical qualified name and language family as a fallback.

### Consequences

- Good: one checklist for a new language, correct monorepo links, results that say which tool and version produced them, a smaller core.
- Bad: Java and Kotlin must move out of the core, the most-used path ([[ISS-008-moving-java-and-kotlin-into-the-jvm-extractor-can-change-the-output]]). One more process start per JVM scan, and fragments of tens of MB through stdout. Extractors must cope with version ranges, and results can differ between machines ([[ISS-009-results-can-differ-between-machines]]).

### Confirmation

Java- and Kotlin-only scans give identical JSON before and after the move, except `generated` and `analysis` ([[QR-006-internal-refactors-do-not-change-the-output]]). Every extractor passes the conformance test in `scripts/check.sh`. CI tests the oldest and newest supported toolchain.

## Pros and Cons of the Options

- **2:** less work, no extra process, but two integration paths stay. Rejected by the maintainer.
- **3:** reproducible, but analyses code with another compiler than the user's build, and impossible for Swift, Go, C# and Python without large downloads.
- **4:** never downloads, but Java-only and TypeScript users lose zero setup. Pinned stays as fallback.
- **5:** one protocol, but one request per identifier, project import and long start-up. Too slow for batch scans. An extractor may still use one internally.
- **6:** exact without a Kotlin compiler, but needs a build and loses source detail. Stays a possible extra mode of `jvm`.

## More Information

Migration, one issue each: contract spec and conformance test (#10), once-per-scan TypeScript (#11), toolchain selection and provenance (#12), `jvm` extractor (#13), exact Kotlin (#7), new languages (#14 to #17). Open questions: [[ISS-005-is-the-extractor-request-sent-on-stdin-or-in-a-file]], [[ISS-006-how-is-the-projects-kotlin-version-read-without-running-gradle]], [[ISS-007-should-the-provenance-record-the-extractors-own-version]]. Original: `docs/adr/0001-language-support.md`.
