---
id: FR-067
type: functional-requirement
title: "Choose Dart source folders and skip files that cannot be analysed"
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
order: 3
goal: []
delivery:
delivery-checked:
---

# Choose Dart source folders and skip files that cannot be analysed

## Rationale

One broken or excluded file must not lose the rest of a package.

## Requirement

Each `pubspec.yaml` folder is scanned in `lib/`, `bin/`, `web/` and `tool/`, and in the test folders `test/` and `integration_test/` (flagged as tests). Generated files (`*.g.dart`, `*.freezed.dart`) are included, nothing below `.dart_tool/` or `build/`. Files that `analysis_options.yaml` excludes are skipped with one note per module. A file that fails to analyse is skipped with a note and never fails the module.

## Acceptance criteria

- [ ] A file listed under `analyzer: exclude:` is missing from the model and the scan prints one note for its module.
- [ ] `integration_test/` files carry the test flag.

## Hierarchy

- **Parent:** [[FR-006-analyse-dart-with-the-dart-analyzer]]
- **Children:** none.
