---
id: ISS-006
type: issue
title: "How is the project's Kotlin version read without running Gradle?"
status: resolved
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: ["[[BB-011-jvm-extractor]]", "[[FR-010-choose-the-toolchain-in-a-fixed-order]]", "[[FR-052-analyse-kotlin-with-the-kotlin-compiler]]"]
tags: [issue]
severity: minor
kind: question
category: architecture
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: 2026-11-27
---

# How is the project's Kotlin version read without running Gradle?

**What's unresolved.** Exact Kotlin should use the project's Kotlin version, which is declared in Gradle files in several ways.

**Affects.** [[FR-052-analyse-kotlin-with-the-kotlin-compiler]], [[FR-010-choose-the-toolchain-in-a-fixed-order]], [[BB-011-jvm-extractor]].

**Context / evidence.** Open question in [[SRC-003-adr-0001-architecture-for-language-support]]: version catalogs, plugins block.

**Options.**
1. Parse the common declarations (plugins block, version catalog) and fall back to the `PATH`.
2. Run Gradle. Exact but slow, and needs the build to work.

**Resolution.** Option 1, decided by the maintainer on 2026-11-27: read the Kotlin plugin version from the `plugins` block or `gradle/libs.versions.toml`, never run Gradle, and fall back to the next step of the toolchain order. Applied in [[FR-052-analyse-kotlin-with-the-kotlin-compiler]] and [[BB-011-jvm-extractor]].
