---
id: FR-006
type: functional-requirement
title: Analyse Dart with the Dart analyzer
status: review
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-014-spec-dart-support]]", "[[SRC-009-supported-languages]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-010-dart-extractor]]"]
priority: Must
release: delivered
lane: backbone
order: 5
goal: []
delivery: outdated
delivery-checked: "2026-11-28T08:27:38Z"
---

# Analyse Dart with the Dart analyzer

## Rationale

Dart and Flutter apps should be as exact as Java.

## Requirement

Dart packages are analysed with the official analyzer, all packages of a scan that use the same Dart SDK ([[FR-010-choose-the-toolchain-in-a-fixed-order]]) in one run, so references between them (pub workspaces, `path:` dependencies) link to the real declarations. Without `.dart_tool/package_config.json` the scan still runs, imports of other packages count as errors and a hint is printed.

## Acceptance criteria

- [ ] `test/checks/dart.py` passes on `test/fixture-dart/`.
- [ ] A reference from the app to `packages/shop_core` links to its declaration, not to an external.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** [[FR-065-map-dart-declarations-to-model-elements]], [[FR-066-derive-dart-dependencies-from-the-analyzer]], [[FR-067-choose-dart-source-folders-and-skip-files-that-cannot-be-analysed]].
