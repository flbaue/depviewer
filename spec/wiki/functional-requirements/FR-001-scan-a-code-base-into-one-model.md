---
id: FR-001
type: functional-requirement
title: Scan a code base into one model
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-008-usage-reference]]", "[[SRC-011-spec-multi-language-architecture]]"]
related: []
tags: [functional-requirement]
notation: prose
level: feature
parent: []
building-blocks: ["[[BB-002-scanner-core]]", "[[BB-007-java-scanner]]", "[[BB-008-kotlin-scanner]]", "[[BB-009-typescript-extractor]]", "[[BB-010-dart-extractor]]"]
priority: Must
release: delivered
lane: backbone
order: 1
goal: ["[[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]]", "[[GOAL-004-one-answer-in-every-interface]]"]
delivery:
delivery-checked:
---

# Scan a code base into one model

## Rationale

Every interface works on the same model ([[GOAL-004-one-answer-in-every-interface]]), and the model is only worth checking against if its dependencies are right ([[QR-004-no-dependency-is-invented]]).

## Requirement

depviewer scans one or more directories, finds the modules and languages in them and analyses each language with its own toolchain into one [[DT-001-model]] of elements and dependencies. A language whose toolchain is missing is skipped. Planned work makes every language an extractor behind one contract ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]).

## Hierarchy

- **Parent:** none.
- **Children:** see `parent:` backlinks.

## History

> Delivered before this wiki was captured: status "done" in [[SRC-001-vision-and-goal-of-depviewer]].
