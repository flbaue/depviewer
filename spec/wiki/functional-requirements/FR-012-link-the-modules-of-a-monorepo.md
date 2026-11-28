---
id: FR-012
type: functional-requirement
title: Link the modules of a monorepo
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-020-github-issue-4-typescript-link-references-between-packages-of-a-monorepo]]", "[[SRC-027-github-issue-11-run-extractors-once-per-scan-typescript-monorepo-links]]"]
related: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-006-toolchain-runner]]", "[[BB-009-typescript-extractor]]"]
priority: Must
release: m2-extractor-contract
lane: backbone
order: 11
goal: []
delivery:
delivery-checked:
---

# Link the modules of a monorepo

## Rationale

TypeScript runs once per module today, so references between packages of a monorepo point to `node_modules` or externals ([[SRC-020-github-issue-4-typescript-link-references-between-packages-of-a-monorepo|issue #4]]).

## Requirement

Each extractor runs once per scan with all modules of its languages and reports the module of every file, so references between modules of one ecosystem resolve to the real declarations.

## Acceptance criteria

- [ ] In a TypeScript monorepo fixture, an import of a sibling workspace package links to its declaration, not to an external.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.
