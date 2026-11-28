---
id: FR-060
type: functional-requirement
title: "Map Kotlin declarations to model elements"
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-012-spec-kotlin-support]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-004-analyse-kotlin-by-name]]"]
building-blocks: ["[[BB-008-kotlin-scanner]]"]
priority: Must
release: delivered
lane: backbone
order: 1
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Map Kotlin declarations to model elements

## Rationale

Kotlin code must show up in the same tree as Java, with the same kinds and qualified names.

## Requirement

Kotlin sources come from `src/{main,test}/kotlin` (test roots flagged), from Java source roots and from folders without the standard layout. Each declaration becomes one [[DT-002-element]]:

| Kotlin | Kind |
|---|---|
| `class`, `data`, `sealed`, `value class` | `class` |
| `interface`, `fun interface` | `interface` |
| `enum class` (entries `enumconst`) | `enum` |
| `annotation class` | `annotation` |
| `object`, `companion object` (unnamed: `Companion`) | `object` |
| `typealias` | `typealias` |
| primary and secondary constructors, named `Order(String, Int)` | `ctor` |
| `fun`, extension functions as `Receiver.name(...)` | `method` |
| `val`/`var`, constructor `val`/`var` parameters | `field` |

Top-level functions and properties are children of their file. Types are qualified `pkg.Outer.Inner`, top-level members belong to the file facade `pkg.FileNameKt` or the `@file:JvmName`. `private`, `protected` and default map to the Java visibilities, `internal` to package level. Local functions and classes are not elements; their references count for the enclosing declaration.

## Acceptance criteria

- [ ] `test/checks/jvm.py` finds an unnamed companion as `Companion` with kind `object`.
- [ ] A top-level function's qualified name starts with its file facade, e.g. `com.example.NotificationsKt`.

## Hierarchy

- **Parent:** [[FR-004-analyse-kotlin-by-name]]
- **Children:** none.
