---
id: FR-003
type: functional-requirement
title: Analyse Java with the JDK compiler
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-009-supported-languages]]", "[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-007-java-scanner]]"]
priority: Must
release: delivered
lane: backbone
order: 2
goal: []
delivery:
delivery-checked:
---

# Analyse Java with the JDK compiler

## Rationale

Java is the main language and the JDK is always there, so Java can be exact at no extra cost.

## Requirement

Java sources are parsed and attributed with javac's Trees API, so overloads, inherited members and static imports resolve the way javac sees them. Library types resolve when their jars are on the classpath ([[FR-008-resolve-library-types-from-the-classpath]]). Without them the scan still works and unresolved references are listed as unresolved externals.

## Acceptance criteria

- [ ] `test/checks/jvm.py` passes on `test/fixture/`.
- [ ] A call to an overloaded method links to the overload javac selects.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.
