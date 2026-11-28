---
id: FR-002
type: functional-requirement
title: Find modules by their build markers
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-008-usage-reference]]", "[[SRC-011-spec-multi-language-architecture]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-003-cli-and-orchestration]]"]
priority: Must
release: delivered
lane: backbone
order: 1
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Find modules by their build markers

## Rationale

Mixed repositories and monorepos hold several modules in several languages, and the user should not have to list them.

## Requirement

Each input directory is searched, skipping `node_modules`, `.dart_tool`, build output and VCS folders, for `src/{main,test}/{java,kotlin}`, `package.json` and `pubspec.yaml`. Each marker gives a [[DT-005-module]] named after `artifactId` from `pom.xml`, the `name` in `package.json` or `pubspec.yaml`, else the folder. A directory without any marker is scanned as a plain Java/Kotlin source root.

## Acceptance criteria

- [ ] A Flutter app next to a Maven backend in one tree gives one Dart and one Java module in one model.
- [ ] A folder of `.java` files without `src/main/java` is still scanned.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.
