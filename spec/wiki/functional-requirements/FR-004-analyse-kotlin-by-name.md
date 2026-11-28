---
id: FR-004
type: functional-requirement
title: Analyse Kotlin by name
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-012-spec-kotlin-support]]", "[[SRC-009-supported-languages]]"]
related: ["[[ISS-004-should-name-based-violations-fail-the-build-by-default]]", "[[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-008-kotlin-scanner]]"]
priority: Must
release: delivered
lane: backbone
order: 3
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Analyse Kotlin by name

## Rationale

Kotlin projects must be scannable with only a JDK ([[CON-001-the-scanner-core-needs-only-a-jdk-17]]), and there is no Kotlin compiler in the JDK.

## Requirement

`.kt` files are read by the built-in parser and references are resolved by name: imports, the same package, declared types of parameters, variables and properties, and call chains. When a name is ambiguous no edge is produced ([[QR-004-no-dependency-is-invented]]). Java and Kotlin reference each other in both directions. `.kts` files and Multiplatform source sets are not scanned.

## Acceptance criteria

- [ ] `test/checks/jvm.py` passes for the Kotlin parts of `test/fixture/`.
- [ ] `NotificationService.INSTANCE.register(...)` from Java links to the Kotlin member, not to *External › (unresolved)*.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** [[FR-060-map-kotlin-declarations-to-model-elements]], [[FR-061-resolve-kotlin-names-in-a-fixed-order]], [[FR-062-link-java-references-to-kotlin-declarations]].

> [!note] Open points
> Results are approximate until [[FR-052-analyse-kotlin-with-the-kotlin-compiler]], see [[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]].
