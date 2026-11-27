---
id: FR-066
type: functional-requirement
title: "Derive Dart dependencies from the analyzer"
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
order: 2
goal: []
delivery: implemented
delivery-checked: "2026-11-27T21:56:32+01:00"
---

# Derive Dart dependencies from the analyzer

## Rationale

The analyzer resolves every identifier, so Dart dependencies are exact.

## Requirement

Every identifier the analyzer resolves gives an edge from the innermost declaring element. Project elements are the target (a `part` resolves to its declaring file). Other packages give an external `<package>` from the `package:<name>/` URI with the top-level name, and `dart:` libraries give `dart:core`, `dart:async` and so on.

Edge kinds: `extends`; `implements`, `with` and `on` are `implements`; function and method invocations are `call`; constructor invocations, with or without `new`/`const`, are `new` to the constructor; getter, setter, field and variable access is `access`; everything else is `uses`. Import, export and part directives give no edge.

## Acceptance criteria

- [ ] A `with` clause gives an `implements` edge to the mixin.
- [ ] A `const` constructor call gives a `new` edge to that constructor.

## Hierarchy

- **Parent:** [[FR-006-analyse-dart-with-the-dart-analyzer]]
- **Children:** none.
