---
id: BB-016
type: building-block
title: Go scanner core
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: []
tags: [building-block]
parent: "[[CTX-001-depviewer-context]]"
drivers: ["[[QR-009-a-project-scans-with-the-depviewer-binary-and-its-own-toolchain]]", "[[CON-013-the-scanner-core-is-go-with-the-standard-library-only]]", "[[QR-006-internal-refactors-do-not-change-the-output]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]", "[[DR-002-the-scanner-core-is-a-static-go-binary]]"]
uses: ["[[BB-009-typescript-extractor]]", "[[BB-010-dart-extractor]]", "[[BB-011-jvm-extractor]]", "[[BB-013-viewer]]", "[[EIF-001-scanned-code-base]]"]
location:
state: target
superseded-by:
---

# Go scanner core

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

The static `depviewer` binary that replaces [[BB-001-depviewer-wrapper-script]] and [[BB-002-scanner-core]]. Same responsibilities as the Java core: discovers modules, runs the extractors, merges their fragments into one model, resolves selectors and writes the viewer or the JSON. Contains no language analysis.

## Interfaces

- **Provides:** the same command line as [[BB-003-cli-and-orchestration]].
- **Needs:** the extractors, started as child processes. Embeds `viewer.html` and the jar of [[BB-011-jvm-extractor]], which it runs with the project's JDK.

## Quality / Performance

Produces the same JSON as the Java core ([[QR-006-internal-refactors-do-not-change-the-output]]).
