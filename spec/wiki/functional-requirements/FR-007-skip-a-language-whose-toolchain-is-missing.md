---
id: FR-007
type: functional-requirement
title: Skip a language whose toolchain is missing
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-011-spec-multi-language-architecture]]", "[[SRC-008-usage-reference]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-006-toolchain-runner]]"]
priority: Must
release: delivered
lane: backbone
order: 6
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Skip a language whose toolchain is missing

## Rationale

A missing Node or Dart must not stop a Java user ([[QR-005-java-projects-scan-with-nothing-but-a-jdk]]).

## Requirement

When the toolchain of a language is not found, its modules are skipped with a note on stderr and the remaining languages are still scanned. It is never a scan failure.

## Acceptance criteria

- [ ] Without `node` on the `PATH`, a repository with a Java and a TypeScript module produces a model with the Java module and a note about TypeScript.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.
