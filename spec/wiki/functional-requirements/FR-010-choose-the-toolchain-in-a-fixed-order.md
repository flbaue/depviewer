---
id: FR-010
type: functional-requirement
title: Choose the toolchain in a fixed order
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-028-github-issue-12-toolchain-selection-user-s-first-pinned-fallback-and-analysis-prov]]"]
related: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]", "[[ISS-006-how-is-the-projects-kotlin-version-read-without-running-gradle]]", "[[ISS-009-results-can-differ-between-machines]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-006-toolchain-runner]]"]
priority: Must
release: m2-extractor-contract
lane: backbone
order: 9
goal: []
delivery:
delivery-checked:
---

# Choose the toolchain in a fixed order

## Rationale

The same toolchain versions as the user's build understand the same syntax and settings, and a pinned fallback keeps zero setup ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]).

## Requirement

For each extractor the toolchain is chosen in this order: an override `DEPVIEWER_<TOOL>`, the project's own version, the tool on the `PATH`, a pinned download where one exists, else the language is skipped ([[FR-007-skip-a-language-whose-toolchain-is-missing]]). A candidate is used only if its version is in the extractor's supported range, which the extractor checks with `--describe`. Two machines can therefore pick different versions. Provenance ([[FR-011-record-the-provenance-of-every-language-and-module]]) shows it, and `DEPVIEWER_<TOOL>` pins one version in CI ([[ISS-009-results-can-differ-between-machines]]).

## Acceptance criteria

- [ ] With a supported `typescript` in the project's `node_modules`, the scan uses it and records `source: project`.
- [ ] An unsupported version is reported and the next candidate is tried.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.
