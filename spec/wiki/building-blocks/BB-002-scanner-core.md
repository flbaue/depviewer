---
id: BB-002
type: building-block
title: Scanner core
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-011-spec-multi-language-architecture]]", "[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: []
tags: [building-block]
parent: "[[CTX-001-depviewer-context]]"
drivers: ["[[FR-001-scan-a-code-base-into-one-model]]", "[[CON-001-the-scanner-core-needs-only-a-jdk-17]]", "[[GOAL-004-one-answer-in-every-interface]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]", "[[DR-002-the-scanner-core-is-a-static-go-binary]]"]
uses: ["[[BB-009-typescript-extractor]]", "[[BB-010-dart-extractor]]", "[[BB-011-jvm-extractor]]", "[[BB-013-viewer]]", "[[EIF-001-scanned-code-base]]", "[[EIF-002-jdk]]"]
location: src/depviewer/
state: current
superseded-by: "[[BB-016-go-scanner-core]]"
---

# Scanner core

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

The Java program behind every command: discovers modules, runs the language analysis, merges the results into one model and writes the viewer or the JSON. Target: no language-specific analysis left in it ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]), then replaced by the [[BB-016-go-scanner-core]] ([[DR-002-the-scanner-core-is-a-static-go-binary]]).

## Interfaces

- **Provides:** the command line ([[BB-003-cli-and-orchestration]]), the model ([[BB-004-model-and-json-writer]]).
- **Needs:** the extractors ([[BB-009-typescript-extractor]], [[BB-010-dart-extractor]], later [[BB-011-jvm-extractor]]) and the viewer template ([[BB-013-viewer]]).
