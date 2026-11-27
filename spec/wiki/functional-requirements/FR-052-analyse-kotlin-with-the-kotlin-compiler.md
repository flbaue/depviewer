---
id: FR-052
type: functional-requirement
title: Analyse Kotlin with the Kotlin compiler
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-023-github-issue-7-kotlin-exact-resolution-with-the-kotlin-compiler-or-a-language-serv]]"]
related: ["[[ISS-006-how-is-the-projects-kotlin-version-read-without-running-gradle]]", "[[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]]", "[[FR-070-read-dependencies-from-compiled-classes]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-051-analyse-more-languages]]"]
building-blocks: ["[[BB-011-jvm-extractor]]"]
priority: Should
release: m4-more-languages
lane: backbone
order: 1
goal: []
delivery:
delivery-checked:
---

# Analyse Kotlin with the Kotlin compiler

## Rationale

Requested in [[SRC-023-github-issue-7-kotlin-exact-resolution-with-the-kotlin-compiler-or-a-language-serv|issue #7]].

## Requirement

Kotlin with the Kotlin compiler is analysed by an extractor inside the `jvm` extractor, using the project's Kotlin version, so Kotlin becomes exact, following the checklist "Adding a language" in AGENTS.md. The project's version is read from the Kotlin plugin in the `plugins` block or from `gradle/libs.versions.toml`, never by running Gradle. If neither declares it, the next step of [[FR-010-choose-the-toolchain-in-a-fixed-order]] applies ([[ISS-006-how-is-the-projects-kotlin-version-read-without-running-gradle]]).

## Acceptance criteria

- [ ] `test/checks/jvm.py` passes with Kotlin recorded as `exact: true`.
- [ ] A call to an overloaded Kotlin function links to the overload the compiler selects.

## Hierarchy

- **Parent:** [[FR-051-analyse-more-languages]]
- **Children:** none.

> [!note] Open points
> Needs its own spec. Order of the new languages is not decided, Go and C# have the most exact official tooling.
