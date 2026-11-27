---
id: CC-002
type: crosscutting-concept
title: Extractor contract
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-011-spec-multi-language-architecture]]"]
related: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]", "[[ISS-005-is-the-extractor-request-sent-on-stdin-or-in-a-file]]"]
tags: [crosscutting-concept]
category: patterns
applies-to: ["[[BB-006-toolchain-runner]]", "[[BB-009-typescript-extractor]]", "[[BB-010-dart-extractor]]", "[[BB-011-jvm-extractor]]"]
drivers: ["[[FR-010-choose-the-toolchain-in-a-fixed-order]]", "[[FR-012-link-the-modules-of-a-monorepo]]", "[[CON-010-extractors-pin-their-dependencies]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
state: target
superseded-by:
---

# Extractor contract

> follows [[arc42]] (section 8)

## Concept


Every extractor follows one versioned contract, to be written as `docs/specs/extractor-contract.md` ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]], issue #10):

- **Input:** one run per scan with a JSON request: modules (name, folder), files per module with test flags, options, and the contract version of the core.
- **Handshake:** `--describe` prints name, contract version, languages and the detected toolchain with version, source and whether it is supported.
- **Output:** the fragment (nodes, externals, edges, errors, module per file) plus `contract`. The core rejects an unknown major version and ignores unknown fields. A missing parent or edge endpoint drops that node or edge with a warning.
- **Diagnostics** go to stderr with a common prefix, the error count goes into the fragment.
- **Conformance test:** a language-neutral checker on each extractor's fixture output: valid shape, parents before children, every edge endpoint exists, unique ids, allowed kinds, lines inside the file, module of every file known.
