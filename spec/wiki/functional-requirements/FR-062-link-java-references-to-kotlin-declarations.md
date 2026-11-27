---
id: FR-062
type: functional-requirement
title: "Link Java references to Kotlin declarations"
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-012-spec-kotlin-support]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-004-analyse-kotlin-by-name]]"]
building-blocks: ["[[BB-007-java-scanner]]", "[[BB-008-kotlin-scanner]]"]
priority: Must
release: delivered
lane: backbone
order: 3
goal: []
delivery:
delivery-checked:
---

# Link Java references to Kotlin declarations

## Rationale

javac cannot see Kotlin sources, so Java references to Kotlin arrive as compile errors and would end up under *External › (unresolved)*.

## Requirement

Java references that javac reports as unresolved are matched to Kotlin declarations by name. The qualified name comes from the Java file's imports, star imports or package.

- A Java reference to a Kotlin type links to that type. `new` links to its constructor if it has exactly one, else to the type.
- A member reference on a Kotlin type links to the Kotlin member of that name, through `INSTANCE`, `Companion` and file facades (`NotificationsKt.format(...)`).
- None of these references appear as unresolved externals such as `?NotificationService.INSTANCE`.

Other generated names Java may call are not matched.

## Acceptance criteria

- [ ] `NotificationService.INSTANCE.register(...)` links to `NotificationService.register(Notifier)`.
- [ ] `NotificationsKt.format(...)` links to the top-level `format(...)`.

## Hierarchy

- **Parent:** [[FR-004-analyse-kotlin-by-name]]
- **Children:** none.
