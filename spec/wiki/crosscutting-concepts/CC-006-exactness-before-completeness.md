---
id: CC-006
type: crosscutting-concept
title: Exactness before completeness
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-012-spec-kotlin-support]]"]
related: []
tags: [crosscutting-concept]
category: under-the-hood
applies-to: ["[[BB-007-java-scanner]]", "[[BB-008-kotlin-scanner]]", "[[BB-009-typescript-extractor]]", "[[BB-010-dart-extractor]]", "[[BB-011-jvm-extractor]]", "[[BB-012-architecture-checker]]"]
drivers: ["[[QR-004-no-dependency-is-invented]]", "[[FR-011-record-the-provenance-of-every-language-and-module]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
state: current
superseded-by:
---

# Exactness before completeness

> follows [[arc42]] (section 8)

## Concept


- A reference that cannot be resolved to exactly one declaration produces no edge ([[QR-004-no-dependency-is-invented]]). Unresolved Java references are kept as *External › (unresolved)* so they stay visible.
- Each result says how exact it is: provenance per language and module ([[FR-011-record-the-provenance-of-every-language-and-module]]), `exact` on every violation ([[FR-038-report-violations-as-versioned-json]]).
- References between ecosystems are not guessed. The core links an external to another fragment's declaration only when both carry the same fully qualified name and language family ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]).
