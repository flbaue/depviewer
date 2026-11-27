---
id: FR-070
type: functional-requirement
title: "Read dependencies from compiled classes"
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-021-github-issue-5-idea-exact-mode-from-compiled-classes-classes-for-java-and-kotlin]]"]
related: ["[[FR-052-analyse-kotlin-with-the-kotlin-compiler]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-011-jvm-extractor]]"]
priority: Could
release: unscheduled
lane: backbone
order: 14
goal: []
delivery:
delivery-checked:
---

# Read dependencies from compiled classes

## Rationale

Class files of a built project hold every call, field access, `new` and type use, resolved by the real compiler and with line numbers, for Java and Kotlin alike. Reading them needs no extra tooling.

## Requirement

An optional mode (working name `--classes`) reads compiled output (`target/classes`, `build/classes/...`) and links the dependencies it finds to the source-based model's files, packages and line spans. Compiler-generated classes (Kotlin companions, lambdas, `WhenMappings`, file facades) are mapped back to their source declarations.

## Acceptance criteria

- [ ] On `test/fixture/` after a build, the Kotlin overload that [[FR-061-resolve-kotlin-names-in-a-fixed-order]] links to the first match is linked to the right overload.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.

> [!note] Open points
> What bytecode loses (inlined constants, some annotations) is not worked out. It stays a requirement of its own, not an option of [[FR-052-analyse-kotlin-with-the-kotlin-compiler]], because it covers Java too and needs a build (decided by the [[STK-004-maintainer]] on 2026-11-27); language servers were rejected as too slow for bulk use.
