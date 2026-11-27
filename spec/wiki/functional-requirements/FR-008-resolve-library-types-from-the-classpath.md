---
id: FR-008
type: functional-requirement
title: Resolve library types from the classpath
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-008-usage-reference]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-003-cli-and-orchestration]]", "[[BB-007-java-scanner]]"]
priority: Should
release: delivered
lane: backbone
order: 7
goal: []
delivery:
delivery-checked:
---

# Resolve library types from the classpath

## Rationale

References to library types are only exact when javac can see the jars.

## Requirement

`target/dependency/*.jar`, `lib/` and `libs/` are put on the classpath automatically. `--cp` adds more. Without the jars, dependencies between the project's own classes are still found and the scan prints the number of compile errors.

## Acceptance criteria

- [ ] After `mvn dependency:copy-dependencies`, library types appear as externals in their package instead of under *External › (unresolved)*.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.
