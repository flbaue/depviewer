---
id: SOL-002
type: solution-strategy
title: Each language is analysed by its own toolchain
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
tags: [solution-strategy]
drivers: ["[[QR-004-no-dependency-is-invented]]", "[[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]]", "[[FR-010-choose-the-toolchain-in-a-fixed-order]]", "[[FR-012-link-the-modules-of-a-monorepo]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
state: target
superseded-by:
---

# Each language is analysed by its own toolchain

> follows [[arc42]] (section 4)

## Strategy


- **No invented dependencies** ([[QR-004-no-dependency-is-invented]], [[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]]) → every language is analysed by its official compiler or analyzer, in an [[DT-019-extractor]] that prints a [[DT-020-fragment]]. Decided in [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]].
- **Correct links inside monorepos** ([[FR-012-link-the-modules-of-a-monorepo]]) → one extractor per [[DT-021-ecosystem]], run once per scan with all modules of its languages. Decided in [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]].
- **Same syntax as the user's build** ([[FR-010-choose-the-toolchain-in-a-fixed-order]]) → the user's toolchain first, a pinned download as fallback, else skip ([[FR-007-skip-a-language-whose-toolchain-is-missing]]). Decided in [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]].
- **Trust is visible** ([[FR-011-record-the-provenance-of-every-language-and-module]]) → provenance per language and module, and `exact` on every violation ([[CC-006-exactness-before-completeness]]).
- Today Java and Kotlin still run inside the core ([[BB-007-java-scanner]], [[BB-008-kotlin-scanner]]). The migration to the `jvm` extractor ([[BB-011-jvm-extractor]]) is planned.
