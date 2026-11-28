---
id: BB-006
type: building-block
title: Toolchain runner
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: ["[[ISS-005-is-the-extractor-request-sent-on-stdin-or-in-a-file]]"]
tags: [building-block]
parent: "[[BB-002-scanner-core]]"
drivers: ["[[FR-007-skip-a-language-whose-toolchain-is-missing]]", "[[FR-010-choose-the-toolchain-in-a-fixed-order]]", "[[FR-011-record-the-provenance-of-every-language-and-module]]", "[[FR-012-link-the-modules-of-a-monorepo]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
uses: ["[[BB-009-typescript-extractor]]", "[[BB-010-dart-extractor]]", "[[BB-011-jvm-extractor]]", "[[BB-005-json-reader]]", "[[BB-004-model-and-json-writer]]", "[[EIF-006-package-registries]]"]
location: src/depviewer/Toolchain.java
state: current
superseded-by:
---

# Toolchain runner

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

`Toolchain`: chooses a language's toolchain in a fixed order and asks the extractor with `--describe` whether it supports it ([[FR-010-choose-the-toolchain-in-a-fixed-order]]), installs the extractor's pinned dependencies on first use, runs the extractor and merges its fragment. Planned: provenance.

## Interfaces

- **Provides:** run an extractor for a set of modules, return its fragment.
- **Needs:** [[BB-009-typescript-extractor]], [[BB-010-dart-extractor]], [[BB-005-json-reader]], [[BB-004-model-and-json-writer]], [[EIF-006-package-registries]] on first use.
