---
id: FR-065
type: functional-requirement
title: "Map Dart declarations to model elements"
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-014-spec-dart-support]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-006-analyse-dart-with-the-dart-analyzer]]"]
building-blocks: ["[[BB-010-dart-extractor]]"]
priority: Must
release: delivered
lane: backbone
order: 1
goal: []
delivery: implemented
delivery-checked: "2026-11-27T21:56:32+01:00"
---

# Map Dart declarations to model elements

## Rationale

Dart and Flutter code must fit the same tree as the other languages.

## Requirement

Files (libraries and parts) sit in the package of their folder (`lib/src/models`).

| Dart | Kind |
|---|---|
| `class`, `abstract`, `sealed class`, `extension type` | `class` |
| `interface class`, `abstract interface class` | `interface` |
| `mixin`, `mixin class` | `mixin` |
| `enum` (values `enumconst`) | `enum` |
| `extension` (unnamed: `extension on Type`) | `extension` |
| `typedef` | `typealias` |
| unnamed, named and factory constructors (`Foo(...)`, `Foo.bar(...)`) | `ctor` |
| methods, operators, top-level functions | `method` |
| fields, getters and setters, top-level variables | `field` |

Top-level functions and variables are children of their file. `_x` names are private, `static` members are static. Qualified names are `<path>#Name` and `<path>#Class.member`. Widgets are classes like any other.

## Acceptance criteria

- [ ] `test/checks/dart.py` finds the mixin, the extension and the factory constructor of `test/fixture-dart/` with these kinds.

## Hierarchy

- **Parent:** [[FR-006-analyse-dart-with-the-dart-analyzer]]
- **Children:** none.
