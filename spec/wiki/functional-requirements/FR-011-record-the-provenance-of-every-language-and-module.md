---
id: FR-011
type: functional-requirement
title: Record the provenance of every language and module
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-028-github-issue-12-toolchain-selection-user-s-first-pinned-fallback-and-analysis-prov]]"]
related: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]", "[[ISS-007-should-the-provenance-record-the-extractors-own-version]]", "[[ISS-009-results-can-differ-between-machines]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-004-model-and-json-writer]]", "[[BB-013-viewer]]"]
priority: Must
release: m2-extractor-contract
lane: backbone
order: 10
goal: []
delivery:
delivery-checked:
---

# Record the provenance of every language and module

## Rationale

Architecture checks and the viewer must be able to tell the user how far a result can be trusted.

## Requirement

The model carries an `analysis` list with one entry per language and set of modules: tool, version, source (`override`, `project`, `path`, `pinned`) and `exact`. The viewer shows it in the header or details. Violations report `exact` from it ([[FR-038-report-violations-as-versioned-json]]).

## Acceptance criteria

- [ ] A scan of `test/fixture/` records `java` with `exact: true` and `kotlin` with `exact: false` until [[FR-052-analyse-kotlin-with-the-kotlin-compiler]].

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.
